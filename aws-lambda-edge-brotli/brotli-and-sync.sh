find -L . \( -name "*.html" -o -name "*.css" -o -name "*.js" -o -name "*.svg" -o -name "*.m3u8" \) \
  ! -path "./node_modules/**" ! -path "**/node_modules/**" -exec brotli -f --keep -9 {} \\;

#synchronise brotli/html files
aws s3 sync . s3://$bucket_name --exclude="*" --include="**/*.html.br" \
  --include="*.html.br" --include="*.html.br" --content-encoding br --content-type="text/html" \
  --exclude="*.ts"

#synchronise brotli/js files
aws s3 sync . s3://$bucket_name --exclude="*" --include="**/*.js.br" \
  --include="*.js.br" --include="*.js.br" --content-encoding br --content-type="application/javascript" \
  --exclude="*.ts"
