from doc2vec.server import Server
import logging

logger = logging.getLogger()
logger.setLevel("INFO")
logger.addHandler(logging.FileHandler("logs"))


application = Server()
