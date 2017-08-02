import gensim
from gensim.similarities.index import AnnoyIndexer

class Model:
    LOCK_VECTORS = 0.0 # modify word embeddings or not
    WORKERS = 8
    EPOCHS = 20 
    # TODO: test more dimensions now that we are doing delete_temporary_training_data
    DIMENSIONS = 50 # more dimensions let server run out of memory
    NUM_TREES = 10 # for nearest neighbor approximation; TODO: test different tree sizes; benchmark
    INDEX = False 
    MIN_COUNT = 5
    INFERENCE = False # no search

    def __init__(self):
        self.data = self.model = self.index = None


    def preprocess(self, document_reader):
        self.data = document_reader
        return self


    def _transform(self, doc):
        text = doc['text']
        docId = doc['id']
        words = gensim.utils.simple_preprocess(text)
        # non-sequential int-tags seem to confuse gensim
        return gensim.models.doc2vec.TaggedDocument(words, [str(docId)])


    def build(self, pretrained_vectors=None):
        if not self.data:
            raise Exception("No data entered to train model on.")

        self.model = gensim.models.doc2vec.Doc2Vec(size=Model.DIMENSIONS, workers=Model.WORKERS, iter=Model.EPOCHS, min_count=Model.MIN_COUNT)
        docs = self.data.open(self._transform)
        self.model.build_vocab(docs)
        self.data.close()

        if pretrained_vectors:
            self.model.intersect_word2vec_format(pretrained_vectors, lockf=Model.LOCK_VECTORS)

        return self


    def train(self, fname):
        if not self.model:
            raise Exception("No model built.")

        docs = self.data.open(self._transform)
        self.model.train(docs, total_examples=self.model.corpus_count, epochs=self.model.iter)
        self.model.delete_temporary_training_data(keep_doctags_vectors=True, keep_inference=Model.INFERENCE)

        self.model.save(fname)

        if Model.INDEX:
            # build annoy indexer
            self.index = AnnoyIndexer(self.model, Model.NUM_TREES)
            self.index.save(f"{fname}.index")

        return self


    @staticmethod
    def load(fname):
        model = Model()
        model.model = gensim.models.Doc2Vec.load(fname, mmap='r')
        model.model.delete_temporary_training_data(keep_doctags_vectors=True, keep_inference=Model.INFERENCE)
        if Model.INDEX:
            model.index = AnnoyIndexer()
            try:
                model.index.load(f"{fname}.index")
                model.index.model = model.model
            except IOError:
                model.index = AnnoyIndexer(model.model, Model.NUM_TREES)
                model.index.save(f"{fname}.index")

        return model


    def lookup(self, tag):
        if not self.model:
            raise Exception("No model built/loaded.")

        if tag not in self.model.docvecs:
            raise KeyError("No document with this tag found..")

        return self.model.docvecs[tag]


    def infer(self, text):
        if not self.model:
            raise Exception("No model built/loaded.")

        if not Model.INFERENCE:
            raise Exception("Inference was disabled.")

        words = gensim.utils.simple_preprocess(text)
        return self.model.infer_vector(words)


    def similar(self, vector, limit):
        if not self.model:
            raise Exception("No model built/loaded.")
        
        if Model.INDEX and self.index:
            results = self.model.docvecs.most_similar([vector], topn=limit, indexer=self.index)
        else:
            results = self.model.docvecs.most_similar([vector], topn=limit)

        return [ {'id': docId, 'similarity': sim } for docId, sim in results]
