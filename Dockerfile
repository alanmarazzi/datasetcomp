FROM clojure:lein-2.8.3-alpine as clj

COPY . /datasetcomp

WORKDIR /datasetcomp

RUN lein deps #&& lein run


FROM conda/miniconda3 as ptn

COPY --from=clj /datasetcomp /datasetcomp

WORKDIR /datasetcomp/bench

RUN conda install --yes --file requirements.txt
    # && rm python.json && python bench.py -p 1 -n 2 -o "python.json"


FROM r-base as rrr

COPY --from=ptn /datasetcomp /datasetcomp

WORKDIR /datasetcomp/bench

RUN echo "install.packages('data.table')" | R --no-save
    # && Rscript fast.R
