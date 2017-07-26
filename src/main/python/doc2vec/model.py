import gensim

class Model:
    LOCK_VECTORS = 0.0
    WORKERS = 4
    EPOCHS = 20
    DIMENSIONS=100
    def __init__(self):
        self.data = self.model = None

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
            raise Error("No data entered to train model on.")

        self.model = gensim.models.doc2vec.Doc2Vec(size=Model.DIMENSIONS, workers=Model.WORKERS, iter=Model.EPOCHS)
        docs = self.data.open()
        self.model.build_vocab(map(self._transform, docs))
        self.data.close()

        if pretrained_vectors:
            self.model.intersect_word2vec_format(pretrained_vectors, lockf=Model.LOCK_VECTORS)

        return self


    def train(self, fname):
        if not self.model:
            raise Error("No model built.")

        docs = self.data.open()
        self.model.train(map(self._transform, docs),
                         total_examples=self.model.corpus_count, epochs=self.model.iter)

        self.model.save(fname)
        return self


    @staticmethod
    def load(fname):
        model = Model()
        model.model = gensim.models.Doc2Vec.load(fname)
        return model


    def lookup(self, tag):
        if not self.model:
            raise Error("No model built/loaded.")

        if tag not in self.model.docvecs:
            raise KeyError("No document with this tag found..")

        return self.model.docvecs[tag]


    def infer(self, text):
        if not self.model:
            raise Error("No model built/loaded.")

        words = gensim.utils.simple_preprocess(text)
        return self.model.infer_vector(words)


    def similar(self, vector, limit):
        if not self.model:
            raise Error("No model built/loaded.")
        
        results = self.model.docvecs.most_similar([vector], topn=limit)
        return [ {'id': docId, 'similarity': sim } for docId, sim in results]


# def test_model(model, data, test_limit=10, chance=0.3):
#     ranks = []
#     comparisons = []
#     i = 0
#     for doc in data:
#         import random
#         if random.random() > chance:
#             continue

#         similar = query_similar(doc['text'], LIMIT, model)
#         ids = [other['id'] for other in similar]
#         comparisons.append((doc['id'], ids))
#         if doc['id'] in ids:
#             rank = ids.index(doc['id'])
#             ranks.append(rank)
#         else:
#             ranks.append(-1)
#         i += 1
#         if i > test_limit:
#             break

#     import collections
#     return json.dumps(collections.Counter(ranks)) + "\n" + json.dumps(comparisons)
