import csv
from operator import itemgetter
# TODO: add parameter: trainingCorpus, infer dimensions from embeddings

class DocumentReader:
    ''' Abstract class for accessing documents (titles, abstracts, combined) from MrDlib.
    '''
    SOURCES = ['abstract', 'title', 'title_abstract']

    def __init__(self, source, language):
        ''' Create a wrapper around DB Access / CSV Dumps / ... that returns text & ids, laoading them lazily.

        Parameters
        ----------
        source - 'abstract' | 'title' | 'title_abstract' : what data to load
        language - iso language code : restrict documents to this language
        '''
        if source not in DocumentReader.SOURCES:
            raise NotImplementedError(f"Source not implemented: {source}")

        self.source = source
        self.language = language
        self.opened = False
        self.prep = None
        
    
    def open(self, preprocessing=None):
        ''' opening should prepare the object for iterating through documents from the beginning
        
        Parameters
        ----------
        repetitions - int : automatically jump to the beginning again when at end of iterator this often; useful when iterator is used as parameter for training with multiple epochs
        '''
        if self.opened:
            raise Exception("Reader already opened.")
        self.opened = True
        if preprocessing is None:
            self.prep = lambda x: x
        else:
            self.prep = preprocessing
        return self


    def close(self):
        ''' close db connection / file handle / ...
        '''
        if not self.opened:
            raise Exception("Reader already closed")
        self.opened = False 
        return self


    def __iter__(self):
        ''' return iterable through data; each call should return a valid iterable that starts from the beginning
        iterators should deliver {'id', 'text'} elements
        '''
        raise NotImplementedError()


class DocumentDumpReader(DocumentReader):
    '''
    '''
    MIN_TEXT_LENGTH=20

    def __init__(self, source, language, fname, config):
        super().__init__(source, language)
        self.csv = self.reader = self.documents = self.prep = None
        self.fname = fname
        self.config = config


    def open(self, preprocessing=None):
        ''' 
        '''
        super().open(preprocessing)
        self.csv = open(self.fname, newline='')
        return self


    def close(self):
        super().close()
        self.csv.close()
        return self


    def __iter__(self):
        self.csv.seek(0)
        self.reader = csv.reader(self.csv, escapechar='\\')

        get_language = get_document_id = get_text = None

        if self.source == 'abstract':
            get_language = itemgetter(int(self.config['abstractLanguageDetectedColumnIndex']))
            get_document_id = itemgetter(int(self.config['abstractDocumentIdColumnIndex']))
            get_text = itemgetter(int(self.config['abstractTextColumnIndex']))

        elif self.source == 'title':
            get_language = itemgetter(int(self.config['titleLanguageDetectedColumnIndex']))
            get_document_id = itemgetter(int(self.config['titleDocumentIdColumnIndex']))
            get_text = itemgetter(int(self.config['titleTextColumnIndex']))

        elif self.source == 'title_abstract':
            get_language = itemgetter(int(self.config['combinedLanguageDetectedColumnIndex']))
            get_document_id = itemgetter(int(self.config['combinedDocumentIdColumnIndex']))
            get_title_text = itemgetter(int(self.config['combinedTitleColumnIndex']))
            get_abstract_text = itemgetter(int(self.config['combinedAbstractColumnIndex']))
            get_text = lambda row: get_title_text(row) + (get_abstract_text(row) or '')
            
        else:
            raise NotImplementedError()

        min_text_length = DocumentDumpReader.MIN_TEXT_LENGTH # int(self.config['abstractMinTextLength'])

        in_language = filter(lambda row: get_language(row) == self.language, self.reader)

        if min_text_length > 0:
            min_length = filter(lambda row: len(get_text(row)) > min_text_length, in_language)
        else:
            min_length = in_language

        samples = map(lambda row: {'text': get_text(row), 'id': get_document_id(row) }, min_length)
        return map(self.prep, samples)
