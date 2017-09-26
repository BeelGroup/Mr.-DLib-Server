from distutils.core import setup
setup(name='mdl-doc2vec', version='0.1', py_modules=['doc2vec'],
      install_requires=['gensim', 'werkzeug', 'mod_wsgi', 'annoy'])
# if this fails, do:
# sudo apt-get install gcc gfortran libblas-dev libatlas-dev apache2-dev
