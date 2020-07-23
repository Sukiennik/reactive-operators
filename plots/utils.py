import math
import os

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def get_directory(module=None, operator=None, method=None):
    if method:
        path = '{}\\{}\\{}'.format(module, operator, method)
        return path.replace(':+forced-', '_')
    elif operator:
        return '{}\\{}'.format(module, operator)
    elif module:
        return '{}'.format(module)
    else:
        return 'complete'


def save_fig(fig, path, ext='png', close=True, verbose=True):
    directory = os.path.split(path)[0]
    filename = "%s.%s" % (os.path.split(path)[1], ext)
    if directory == '':
        directory = '.'

    if not os.path.exists(directory):
        os.makedirs(directory)

    savepath = os.path.join(directory, filename)

    if verbose:
        print("Saving figure to '%s'..." % savepath),

    fig.savefig(savepath, bbox_inches='tight')

    if close:
        plt.close(fig)

    if verbose:
        print("Done")


def should_plot_log(values):
    for _, scores in values.iterrows():
        lowest = scores[0]
        highest = scores[len(scores) - 1]
        should_log = highest / lowest > 1000
        if should_log:
            return True
    return False


def vectorized_mean(group):
    return group.tolist()

    lg = group.tolist()
    lg_len = len(lg)
    size = 1 if lg_len == 1 else 4
    chunked = list(chunks(lg, size))

    multiple_lists = chunked
    arrays = [np.array(x) for x in multiple_lists]
    mean_list = [np.mean(k) for k in zip(*arrays)]

    if size == 1 and math.isnan(mean_list[0]):
        return ['N/A']
    elif group.name == 'times':
        return [int(x) for x in mean_list]
    else:
        return mean_list


def chunks(lst, n):
    for i in xrange(0, len(lst), n):
        yield lst[i:i + n]


def map_array_to_mb(array):
    return array.apply(lambda series: map(lambda x: bytes_to(x, 'm'), series))


def bytes_to(bytes, to, bsize=1024):
    a = {'k': 1, 'm': 2, 'g': 3, 't': 4, 'p': 5, 'e': 6}
    r = float(bytes)
    for i in range(a[to]):
        r = r / bsize
    return r


def get_benchmark_type(method):
    if 'total' in method:
        return 'Memory GC-Total'
    elif 'after' in method:
        return 'Memory GC-After'
    elif 'TimeMillis' in method:
        return 'Memory GC-Time'
    else:
        return 'Performance'


def get_benchmark_method_type(method):
    if 'singleEachOnIO' in method:
        return 'single IO'
    elif 'single' in method:
        return 'single'
    elif 'multiEachOnIo' in method:
        return 'multi IO'
    elif 'multi' in method:
        return 'multi'


def full_print(x):
    with pd.option_context('display.max_rows', None, 'display.max_columns', None):
        print(x)


def calculate_missing_data(results, operators, penalty = 2):
    df = results

    for operator in operators:
        operator
        series = df[df['benchmark'].str.contains(operator)]

        akka = series[series['benchmark'].str.contains('Akka')]
        rx = series[series['benchmark'].str.contains('Akka')]
        reactor = series[series['benchmark'].str.contains('Akka')]

        if len(akka):
            akka.iloc[0, akka.columns.get_loc('benchmark')] = 'aa'
        print akka

def reconstruct():
    return
