import csv
from operator import itemgetter

class DocumentReader:
    ''' Abstract class for accessing documents (titles, abstracts, combined) from MrDlib.
    '''

    def __init__(self, mode, language):
        ''' Create a wrapper around DB Access / CSV Dumps / ... that returns text & ids, laoading them lazily.

        Parameters
        ----------
        mode - 'abstract' | 'title' | 'combined' : what data to load
        language - iso language code : restrict documents to this language
        '''
        if mode != 'abstract':
            raise NotImplementedError(f"Mode not implemented: {mode}")
        self.mode = mode
        self.language = language
        self.opened = False
        
    
    def open(self, repetitions=1):
        ''' opening should prepare the object for iterating through documents from the beginning
        
        Parameters
        ----------
        repetitions - int : automatically jump to the beginning again when at end of iterator this often; useful when iterator is used as parameter for training with multiple epochs
        '''
        if self.opened:
            raise Error("Reader already opened.")
        self.opened = True
        self.repetitions = repetitions
        return self


    def close(self):
        ''' close db connection / file handle / ...
        '''
        if not self.opened:
            raise Error("Reader already closed")
        self.opened = False 
        return self


    def __iter__(self):
        ''' subclasses only need to implement __next__ to support iterator interface
        '''
        if self.opened:
            return self
        else:
            raise Error("Must open DocumentReader before iterating.")


    def __next__(self):
        ''' DocumentReader instances are iterable, should deliver {'id', 'text'} elements
        
        this method should honor self.repetitions set by self.open()
        '''
        raise NotImplementedError()



class DocumentDumpReader(DocumentReader):
    '''
    '''
    MIN_TEXT_LENGTH=50

    def __init__(self, mode, language, fname, config):
        super().__init__(mode, language)
        self.csv = self.reader = self.documents = None
        self.fname = fname
        self.config = config
        self.repetitions = 0


    def open(self, repetitions=1):
        super().open()
        self.csv = open(self.fname, newline='')
        self.reader = csv.reader(self.csv, escapechar='\\')

        if self.mode == 'abstract':
            get_language = itemgetter(int(self.config['abstractLanguageDetectedColumnIndex']))
            get_document_id = itemgetter(int(self.config['abstractDocumentIdColumnIndex']))
            get_text = itemgetter(int(self.config['abstractTextColumnIndex']))
            min_text_length = DocumentDumpReader.MIN_TEXT_LENGTH # int(self.config['abstractMinTextLength'])

            in_language = filter(lambda row: get_language(row) == self.language, self.reader)

            if min_text_length > 0:
                min_length = filter(lambda row: len(get_text(row)) > min_text_length, in_language)
            else:
                min_length = in_language

            samples = map(lambda row: {'text': get_text(row), 'id': get_document_id(row) }, min_length)
            self.documents = samples


        return self


    def close(self):
        super().close()
        self.csv.close()


    def __next__(self):
        if not self.opened:
            raise Error("Reader not opened")

        # repeat iterating through csv
        try:
            result = next(self.documents)
            return result
        except StopIteration:
            if self.repetitions > 1:
                self.close()
                self.open(self.repetitions - 1)
                return self.__next__()
            else:
                raise StopIteration()
