from .data import DocumentDumpReader
from .model import Model

import threading
import datetime
import configparser
import json
import logging

from werkzeug.wrappers import Request, Response
from werkzeug.routing import Map, Rule

logger = logging.getLogger(__name__)

class Server:
    LIMIT=6
    LANGUAGES=['en', 'de']
    AUTO_LOAD=['en']
    MODES=['title', 'abstract']
    DEFAULT_MODE='abstract'
    def __init__(self):
        self.routes = Map([
            Rule('/search/<query>', endpoint='search'),
            Rule('/train', endpoint='train'),
            Rule('/load', endpoint='load'),
            Rule('/similar/<docId>', endpoint='similar')
        ])
        self.endpoints = {
            'search': self.search,
            'similar': self.related, 
            'train': self.train,
            'load': self.load
        }
        self.models = {}
        self.config = None
        self.read_config()

        for language in Server.AUTO_LOAD:
            self.load_model_task(language, Server.DEFAULT_MODE)
        
    def read_config(self, fname='config.properties'):
        ''' Read standard config file or file given by command line argument. Return as dictionary. Also parse other command line arguments.
        >>> c = read_config()
        >>> 'db_host' in c
        True
        >>> 'password' in c
        True
        '''
        config = configparser.ConfigParser()
        config.read(fname)
        self.config = config['MrDlib']
        return self

    def dispatch(self, request):
        adapter = self.routes.bind_to_environ(request.environ)
        endpoint, values = adapter.match()

        if endpoint in self.endpoints:
            try:
                return self.endpoints[endpoint](request, **values)
            except Exception as e:
                logger.error(f"Request {request} failed with error: {e}")
                return Response(f"Request failed with error: {e}", status=500, mimetype='text/plain')
        else:
            return Response('Route not found.', status=404, mimetype='text/plain')


    def search(self, req, query):
        language = req.args.get('language', 'en')
        mode = req.args.get('mode', Server.DEFAULT_MODE)

        if language not in Server.LANGUAGES:
            return Response('Language code not valid / supported', status=400, mimetype='text/plain')
        if mode not in Server.MODES:
            return Response('Mode not valid / supported', status=400, mimetype='text/plain')
        model_id = f"{language}_{mode}"

        if model_id not in self.models:
            return Response('No model for this language-mode-combination found.', status=501, mimetype='text/plain')

        limit = int(req.args.get('limit', Server.LIMIT))
        model = self.models[model_id]
        vector = model.infer(query)
        results = model.similar(vector, limit)
        return Response(json.dumps(results), mimetype='application/json')
            


    def related(self, req, docId):
        language = req.args.get('language', 'en')
        mode = req.args.get('mode', Server.DEFAULT_MODE)

        if language not in Server.LANGUAGES:
            return Response('Language code not valid / supported', status=400, mimetype='text/plain')

        if mode not in Server.MODES:
            return Response('Mode not valid / supported', status=400, mimetype='text/plain')
        model_id = f"{language}_{mode}"

        if model_id not in self.models:
            return Response('No model for this language-mode-combination found.', status=501, mimetype='text/plain')

        limit = int(req.args.get('limit', Server.LIMIT))
        model = self.models[model_id]
        try:
            vector = model.lookup(docId)
            results = model.similar(vector, limit)
            return Response(json.dumps(results), mimetype='application/json')
        except KeyError:
            return Response('No such document found.', status=404, mimetype='text/plain')


    def train_model_task(self, language, mode):
        logger.info(f"Starting training doc2vec for {language} / {mode} @ {datetime.datetime.now()}.")
        data = DocumentDumpReader(mode, language, f"dump_{mode}", self.config)
        model = Model()
        model_id = f"{language}_{mode}"
        model.preprocess(data).build(f"vectors_{language}").train(f"model_{model_id}")
        self.models[model_id] = model
        logger.info(f"Finished training for {language} / {mode} @ {datetime.datetime.now()}. Saving...")


    def train(self, req):
        if 'language' not in req.args:
            return Response('Language parameter not provided.', status=400, mimetype='text/plain')
        if 'mode' not in req.args:
            return Response('Mode parameter not provided.', status=400, mimetype='text/plain')
        language = req.args.get('language', 'en')
        mode = req.args.get('mode', Server.DEFAULT_MODE)

        if mode not in Server.MODES:
            return Response('Mode not valid / supported', status=400, mimetype='text/plain')
        if language not in Server.LANGUAGES:
            return Response('Language code not valid / supported', status=400, mimetype='text/plain')

        thread = threading.Thread(target=self.train_model_task, args=(language,mode), daemon=False)
        thread.start()
        return Response('Started training.', status=200, mimetype='text/plain')

    def load_model_task(self, language, mode):
        logger.info(f"Starting loading model for {language} / {mode} @ {datetime.datetime.now()}")
        model_id = f"{language}_{mode}"
        self.models[model_id] = Model.load(f"model_{model_id}")
        logger.info(f"Loaded model for {language} / {mode} @ {datetime.datetime.now()}")


    def load(self, req):
        if 'language' not in req.args:
            return Response('Language parameter not provided.', status=400, mimetype='text/plain')

        if 'mode' not in req.args:
            return Response('Mode parameter not provided.', status=400, mimetype='text/plain')
        language = req.args.get('language', 'en')
        mode = req.args.get('mode', Server.DEFAULT_MODE)

        if language not in Server.LANGUAGES:
            return Response('Language code not valid / supported', status=400, mimetype='text/plain')

        if mode not in Server.MODES:
            return Response('Mode not valid / supported', status=400, mimetype='text/plain')
        thread = threading.Thread(target=self.load_model_task, args=(language,mode), daemon=False)
        thread.start()
        return Response(f'Started loading model.', status=200, mimetype='text/plain') 


    def __call__(self, environ, start_response):
        request = Request(environ)
        response = self.dispatch(request)
        return response(environ, start_response)
        

