#!/bin/env python3
import configparser
import sys
import os
import json
import threading
import datetime
import csv
import logging

logger = logging.getLogger()
logger.setLevel("INFO")
logger.addHandler(logging.StreamHandler())

from werkzeug.wrappers import Request, Response
from werkzeug.serving import run_simple
from werkzeug.routing import Map, Rule

from sqlalchemy import create_engine, MetaData, Table
from sqlalchemy.sql import select

import gensim

# dump data to csv using
# >mysqldump --user=root --password --host=localhost --tab=. --fields-terminated-by="," --fields-enclosed-by='"' --fields-escaped-by=\\ "mrdlib" "document_abstract"
# (for abstracts)
# and
# >mysqldump --user=root --password --host=localhost --tab=. --fields-terminated-by="," --fields-enclosed-by='"' --fields-escaped-by=\\ "mrdlib" "document"
# for documents

# TODO
# write tests & doc, run them
# annoy indexer
# deploy properly

# test via doctest

def read_config():
    ''' Read standard config file or file given by command line argument. Return as dictionary. Also parse other command line arguments.
    >>> c = read_config()
    >>> 'db_host' in c['MrDlib']
    True
    >>> 'password' in c['MrDlib']
    True
    '''
    config = configparser.ConfigParser()
    if len(sys.argv) > 1 and os.path.isfile(sys.argv[1]):
        global CONFIG_LOCATION
        CONFIG_LOCATION = sys.argv[1]

    config.read(CONFIG_LOCATION)
    c = config['MrDlib']

    # TODO parse command line arguments
    return c


def open_data_source_mysql(mode):
    ''' connect to mysql to query data
    '''
    if mode != 'abstract':
        raise NotImplementedError(f"Mode not implemented: {mode}")
    path = 'mysql+pymysql://{}:{}@{}/{}'.format(
        CONFIG['user'], CONFIG['password'], CONFIG['db_host'], CONFIG['db'])

    db = create_engine(path)
    return db


def open_data_source_csv(mode):
    ''' read data from csv dump
    '''
    if mode != 'abstract':
        raise NotImplementedError(f"Mode not implemented: {mode}")

    csvfile = open(INPUT_FILE, newline='')
    return csv.reader(csvfile, escapechar='\\'), csvfile


def open_data_source(mode='abstract'):
    return open_data_source_csv(mode)


def close_data_source_csv(source):
    csvreader, csvfile = source
    close(csvfile)


def close_data_source(source):
    close_data_source_csv(source)



def get_documents_from_db(db, language, mode):
    ''' query for all documents from mysql with a language and stream relevant fields. Return as iterator.
    >>> next(get_documents_from_db(open_data_source_mysql()))
    {'text': 'Introducing Mr. DLib, a Machine-readable Digital Library', 'id': 1}
    '''
    metadata = MetaData(db)
    documents = Table(CONFIG['documents'], metadata, autoload=True)
    start = 0

    def fetch_batch(offset):
        query = select([documents.c.title, documents.c.document_id])\
            .where(documents.c.language_detected == language)\
            .order_by(documents.c.document_id.asc())\
            .limit(BATCH_SIZE)\
            .offset(offset)
        results = db.connect().execute(query)
        for doc in results:
            yield {'text': doc[0], 'id': doc[1]}

    empty = False
    while not empty:
        data = fetch_batch(start)
        start += BATCH_SIZE

        empty = True
        for result in data:
            empty = False
            yield result


def get_documents_from_dump(source, language, mode):
    ''' filter documents from csv dump and extract relevant fields. Return as iterator
    Parameters
    ----------
    csvreader : csv.reader() - from open_data_source_csv
    language : string - iso code for filtering
    mode : title | abstract | combined 
    >>> next(get_documents_from_dump(open_data_source_csv('title'), 'en', 'title'))
    {'text': 'Introducing Mr. DLib, a Machine-readable Digital Library', 'id': 1}
    >>> next(get_documents_from_dump(open_data_source_csv('abstract'), 'en', 'abstract'))
    {'text': 'In this demonstration-paper we present Mr. DLib, a machine readable digital library. Mr. DLib provides access to several millions of articles in full-text and their metadata in XML and JSON format via a RESTful Web Service. In addition, Mr. DLib provides related documents for given academic articles. The service is intended to serve researchers who need bibliographic data and full-text of scholarly literature for their analyses (e.g. impact and trend analysis); providers of academic services who need additional information to enhance their own services (e.g. literature recommendations); and providers who want to build their own services based on data from Mr. DLib.', 'id': 1}
    '''
    csvreader, csvfile = source

    if mode != 'abstract':
        raise NotImplementedError(f"Mode not implemented: {mode}")

    from operator import itemgetter
    get_language = itemgetter(int(CONFIG['abstractLanguageDetectedColumnIndex']))
    get_document_id = itemgetter(int(CONFIG['abstractDocumentIdColumnIndex']))
    get_text = itemgetter(int(CONFIG['abstractTextColumnIndex']))

    in_language = filter(lambda row: get_language(row) == language, csvreader)
    samples = map(lambda row: {'text': get_text(row), 'id': get_document_id(row) }, in_language)
    return samples


def get_documents(source, language='en', mode='abstract'):
    return get_documents_from_dump(source, language, mode)


def preprocess_documents(documents):
    ''' convert documents to gensim's input format, lazily
    >>> next(preprocess_documents(get_documents(open_data_source())))
    TaggedDocument(['introducing', 'mr', 'dlib', 'machine', 'readable', 'digital', 'library'], [1])
    '''
    # i = 0
    for doc in documents:
        text = doc['text']
        docId = doc['id']
        words = gensim.utils.simple_preprocess(text)
        # i += 1
        # if i > 1000:
        #     break
        yield gensim.models.doc2vec.TaggedDocument(words, [str(docId)]) # non-sequential int-tags seem to confuse gensim
            

def build_model(data, language):
    model = gensim.models.doc2vec.Doc2Vec(size=DIMENSIONS, workers=4)
    model.build_vocab(data)
    model.intersect_word2vec_format(f"{VECTOR_FILE}_{language}", lockf=LOCK_VECTORS)
    return model

def train_model(model, data, language):
    model.train(data, total_examples=model.corpus_count, epochs=model.iter)
    model.save(f"{OUTPUT_FILE}_{language}")
    return model


def query_similar(query, limit, model):
    words = gensim.utils.simple_preprocess(query)
    vector = model.infer_vector(words) 
    results = model.docvecs.most_similar([vector], topn=limit)
    return [ {'id': docId, 'similarity': sim } for docId, sim in results]


def related_docs(docId, limit, model):
    results = model.docvecs.most_similar([str(docId)], topn=limit)
    return [ {'id': docId, 'similarity': sim } for docId, sim in results]


def search_server(req, query):
    language = req.args.get('language', 'en')
    if language not in MODELS:
        return Response('No model for this language found.', status=501, mimetype='text/plain')
    limit = req.args.get('limit', LIMIT)

    results = query_similar(query, limit, MODELS[language])
    return Response(json.dumps(results), mimetype='application/json')


def related_server(req, docId):
    language = req.args.get('language', 'en')
    if language not in MODELS:
        return Response('No model for this language found.', status=501, mimetype='text/plain')
    limit = req.args.get('limit', LIMIT)

    results = related_docs(docId, limit, MODELS[language])
    return Response(json.dumps(results), mimetype='application/json')


def train_model_task(language):
    print(f"Starting training doc2vec for {language} @ {datetime.datetime.now()}.")
    source = open_data_source()
    docs = preprocess_documents(get_documents(source, language))
    model = build_model(docs, language)
    close_data_source(source)

    # once-only generator - rebuild
    source = open_data_source()
    docs = preprocess_documents(get_documents(source, language))
    model = train_model(model, docs, language)
    close_data_source(source)

    global MODELS
    MODELS[language] = model
    print(f"Finished training for {language} @ {datetime.datetime.now()}. Saving...")


def training_server(req):
    if 'language' not in req.args:
        return Response('Language parameter not provided.', status=400, mimetype='text/plain')

    language = req.args.get('language', 'en')
    thread = threading.Thread(target=train_model_task, args=(language,), daemon=False)
    thread.start()
    return Response('Started training.', status=200, mimetype='text/plain')


def load_server(req):
    if 'language' not in req.args:
        return Response('Language parameter not provided.', status=400, mimetype='text/plain')
    language = req.args.get('language', 'en')

    global MODELS
    try:
        MODELS[language] = gensim.models.Doc2Vec.load(f"{OUTPUT_FILE}_{language}")
        return Response('Model loaded.', status=200, mimetype='text/plain')
    except Exception as e:
        return Response('Could not load model: ' + str(e), status=500, mimetype='text/plain')


def dispatch_request(request):
    adapter = ROUTES.bind_to_environ(request.environ)
    endpoint, values = adapter.match()

    if endpoint in ENDPOINTS:
        return ENDPOINTS[endpoint](request, **values)
    else:
        return Response('Route not found.', status=404, mimetype='text/plain')


def wsgi_app(environ, start_response):
    request = Request(environ)
    response = dispatch_request(request)
    return response(environ, start_response)


if __name__ == '__main__':
    global CONFIG_LOCATION, OUTPUT_FILE, BATCH_SIZE, DIMENSIONS, LIMIT, MODELS,\
        CONFIG, ROUTES, ENDPOINTS, INPUT_FILE, VECTOR_FILE, LOCK_VECTORS
    CONFIG_LOCATION = '../resources/config.properties'
    OUTPUT_FILE = 'model'
    INPUT_FILE = 'dump'
    VECTOR_FILE = 'vectors'
    BATCH_SIZE = 1000
    DIMENSIONS=50
    LIMIT=3
    LOCK_VECTORS=0.0 # don't change word vectors

    ROUTES = Map([
        Rule('/search/<query>', endpoint='search'),
        Rule('/train', endpoint='train'),
        Rule('/load', endpoint='load'),
        Rule('/similar/<docId>', endpoint='similar')
    ])
    ENDPOINTS = {
        'search': search_server,
        'similar': related_server, 
        'train': training_server,
        'load': load_server
    }

    MODELS = {}
    CONFIG = read_config()
    run_simple('localhost', CONFIG['doc2vecServicePort'], wsgi_app)
