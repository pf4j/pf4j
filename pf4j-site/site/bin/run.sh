#!/bin/sh
APP_DIR="$(dirname $0)/.."
cd ${APP_DIR}

SERVE_FILE="_serve-${RANDOM}.yml"
cat > ${SERVE_FILE} <<-EOF
	url: http://localhost:4000
EOF

jekyll serve -w --config _config.yml,${SERVE_FILE} $@
rm ${SERVE_FILE}
