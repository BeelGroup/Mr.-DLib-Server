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


    def open(self):
        super().open()
        self.csv = open(self.fname, newline='')
        self.reader = csv.reader(self.csv, escapechar='\\')

        if self.mode == 'abstract':
            get_language = itemgetter(int(self.config['abstractLanguageDetectedColumnIndex']))
            get_document_id = itemgetter(int(self.config['abstractDocumentIdColumnIndex']))
            get_text = itemgetter(int(self.config['abstractTextColumnIndex']))
            min_text_length = DocumentDumpReader.MIN_TEXT_LENGTH # int(self.config['abstractMinTextLength'])

            in_language = filter(lambda row: get_language(row) == language, csvreader)
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

        return next(self.documents)

    
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
