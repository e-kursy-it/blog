find -L . \
  \( -name "*.html" -o -name "*.css" -o -name "*.js" -o -name "*.svg" -o -name "*.m3u8" \) \
  ! -path "./node_modules/**" ! -path "**/node_modules/**" -exec gzip -f --keep --best {} \\;

#synchronise non-compressed files
aws s3 sync . s3://$bucket_name --exclude="*.gz" --exclude="**/*.gz" \
  --exclude=".git/**" --exclude="*.DS_Store" --exclude=".gitignore" \
  --exclude="*tsconfig.json" --exclude="node_modules/**" --exclude="**/node_modules/**" \
  --exclude="package.json" --exclude="*.ts"

#synchronise gziped files
aws s3 sync . s3://$bucket_name --exclude="*" --include="*.gz" \ --include="**/*.gz" \
  --exclude="*.ts" --content-encoding gzip

