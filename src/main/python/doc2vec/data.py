import csv
from operator import itemgetter


class DocumentReader:

    def __init__(self, mode, language):
        if mode != 'abstract':
            raise NotImplementedError(f"Mode not implemented: {mode}")
        self.mode = mode
        self.language = language
        self.opened = False
        
    
    def open(self):
        if self.opened:
            raise Error("Reader already opened.")
        self.opened = True
        return self


    def close(self):
        if not self.opened:
            raise Error("Reader already closed")
        self.opened = False 
        return self


    def __iter__(self):
        if self.opened:
            return self
        else:
            raise Error("Must open DocumentReader before iterating.")


    def __next__(self):
        raise NotImplementedError()



class DocumentDumpReader(DocumentReader):
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
        self.repetitions = repetitions

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
            if repetitions > 1:
                self.close()
                self.open(self.repetitions - 1)
                return self.__next__()
            else:
                raise StopIteration()
