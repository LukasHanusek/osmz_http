


URL:

/ - index.html
/index.html - index.html
//waterfall.jpg - waterfall.jpg obrázek
/cgi-bin/cat /proc/cpuinfo - provedení pøíkazu
/camera/snapshot - poslední snapshot z kamery
/camera/stream - motion jpeg stream


Max threads:
Omezení poètu vláken je øešeno semaphore, pøi nastavení nového omezení tak zùstanou souèasná spojení (streamy) bežet a nový
limit bude limitovat jen nové spojení vytvoøená od nastavení nového limitu. Souèasná spojení zùstanou otevøena.