#!/bin/sh

# แทนที่ environment variables
envsubst < /usr/share/nginx/html/config.template.js > /usr/share/nginx/html/config.js

# Start nginx
exec nginx -g 'daemon off;'