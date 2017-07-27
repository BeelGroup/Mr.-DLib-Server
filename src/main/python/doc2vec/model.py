import gensim
from gensim.similarities.index import AnnoyIndexer

class Model:
    LOCK_VECTORS = 0.0
    WORKERS = 8
    EPOCHS = 20
    DIMENSIONS=100
    NUM_TREES = 100

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

        self.model = gensim.models.doc2vec.Doc2Vec(size=Model.DIMENSIONS, workers=Model.WORKERS, iter=Model.EPOCHS)
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

        self.model.save(fname)

        # build annoy indexer
        self.index = AnnoyIndexer(self.model, Model.NUM_TREES)
        self.index.save(f"{fname}.index")
        return self


    @staticmethod
    def load(fname):
        model = Model()
        model.model = gensim.models.Doc2Vec.load(fname)

        model.index = AnnoyIndexer()
        model.index.load(f"{fname}.index")
        model.index.model = model.model

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

        words = gensim.utils.simple_preprocess(text)
        return self.model.infer_vector(words)


    def similar(self, vector, limit):
        if not self.model:
            raise Exception("No model built/loaded.")
        
        results = self.model.docvecs.most_similar([vector], topn=limit)
        return [ {'id': docId, 'similarity': sim } for docId, sim in results]
