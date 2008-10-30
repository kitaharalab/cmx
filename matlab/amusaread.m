function [data, attrs, header, fmt] = amusaread(filename)

import jp.crestmuse.cmx.amusaj.filewrappers.*

result = SimpleAmusaXMLReader2.readfile(filename);
header = result.header;
fmt = result.format;
n = result.attrs.length;
for i = 1 : n
  attrs{i} = result.attrs.get(i-1);
  if (strcmp(fmt, 'array'))
    data{i} = decodeArray(result.data.get(i-1), ...
                          str2num(attrs{i}.get('frames')), ...
                          str2num(attrs{i}.get('dim')));
  elseif (strcmp(fmt, 'peaks'))
    data{i} = decodePeaks(result.data.get(i-1), ...
                          str2num(attrs{i}.get('frames')));
  else
    error('unknown format');
  end
end

function data = decodeArray(text, nFrames, dim)

data = strread(text);

function data = decodePeaks(text, nFrames)

X = strread(text);

nTotalPeaks = sum(X(:, 1))
time = zeros(nTotalPeaks, 1);
freq = zeros(nTotalPeaks, 1);
power = zeros(nTotalPeaks, 1);
phase = zeros(nTotalPeaks, 1);
iid = zeros(nTotalPeaks, 1);
ipd = zeros(nTotalPeaks, 1);

k = 1;
for n = 1 : nFrames
  nPeaks = X(n, 1);
  for i = 1 : nPeaks
    time(k) = n;
    freq(k) = X(n, 5 * i - 3);
    power(k) = X(n, 5 * i - 2);
    phase(k) = X(n, 5 * i - 1);
    iid(k) = X(n, 5 * i);
    ipd(k) = X(n, 5 * i + 1);
    k = k + 1;
  end
  fprintf(1, '.');
end
time(k : end) = [];   data.time = time;
freq(k : end) = [];   data.freq = freq;
power(k : end) = [];  data.power = power;
phase(k : end) = [];  data.phase = phase;
iid(k : end) = [];    data.iid = iid;
ipd(k : end) = [];    data.ipd = ipd;
