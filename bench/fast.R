require(data.table)

start <- Sys.time()
d <- fread('../sampledata.txt', sep = '\t')
stop <- Sys.time()
print(stop - start) #  0.8201072 secs
print(c("data.table ", object.size(d))) # 182411664

start <- Sys.time()
d <- read.delim('../sampledata.txt', sep = '\t')
stop <- Sys.time()
print(stop - start) # 14.29843 secs/11.884 secs
print(c("regular", object.size(d))) # 144325688/143999752
