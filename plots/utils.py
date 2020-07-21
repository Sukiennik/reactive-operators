import os
import matplotlib.pyplot as plt
import numpy as np

def get_directory(module, operator, method):
    path = '{}\\{}\\{}'.format(module, operator, method)
    return path.replace(':+forced-', '_')


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
    lg = group.tolist()
    chunked = list(chunks(lg, 4))

    multiple_lists = chunked
    arrays = [np.array(x) for x in multiple_lists]
    mean_list = [np.mean(k) for k in zip(*arrays)]

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
