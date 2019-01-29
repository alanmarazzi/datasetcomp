import sys
import pandas as pd
import numpy as np
import modin.pandas as md
import perf
from io import StringIO
import warnings
warnings.filterwarnings('ignore')

runner = perf.Runner()
setup = "from __main__ import numpy_reader, pandas_reader, modin_reader"

data = '../sampledata.txt'

def numpy_reader():
    return np.genfromtxt(data, delimiter='\t')

def pandas_reader():
    return pd.read_csv(data, sep='\t')

def modin_reader():
    return md.read_csv(data, sep='\t')

runner.timeit("NumPy", "numpy_reader()", setup=setup) # 21.1 sec
runner.timeit("Pandas", "pandas_reader()", setup=setup) # 2.69 sec
runner.timeit("Modin", "modin_reader()", setup=setup) # 851 ms

#print("Numpy size in B: ", sys.getsizeof(numpy_reader())) # 286367992
#print("Pandas size in B: ", sys.getsizeof(pandas_reader())) # 882930424
#print("Modin size in B: ", sys.getsizeof(modin_reader())) # 882930424
