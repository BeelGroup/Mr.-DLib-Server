#!/bin/bash
source /home/admin/miniconda3/bin/activate python36
mod_wsgi-express start-server wsgi.py --httpd-executable=/usr/sbin/apache2 --port=9001 --host=localhost --processes=1
