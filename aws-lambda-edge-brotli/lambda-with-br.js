exports.handler = (event, context, callback) => {
  const request = event.Records[0].cf.request;
  const headers = request.headers;
  if (headers && (
      request.uri.endsWith('.css') ||
      request.uri.endsWith('.html') ||
      request.uri.endsWith('.js') ||
      request.uri.endsWith('.svg') ||
      request.uri.endsWith('.m3u8')
   )) {
    let gz = false;
    let br = false;

    const ae = headers['accept-encoding'];
    if (ae) {
      for (let i = 0; i < ae.length; i++) {
        const value = ae[i].value;
        const bits = value.split(/\s*,\s*/);
        if (bits.indexOf('br') !== -1) {
          br = true;
          break;
        } else if (bits.indexOf('gzip') !== -1) {
          gz = true;
          break;
        }
      }
    }

    // If br is supported use .br sufffix, .gz for gzip :)
    if (br) request.uri += '.br';
    else if (gz) request.uri += '.gz';
  }

  callback(null, request);
};
