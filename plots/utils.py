import math
import os
import re

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def get_filename_suffix(name):
    if 'gc-mem.total' in name:
        return '_gc_memory_total'
    elif 'gc-mem.used.after' in name:
        return '_gc_memory_after'
    elif 'gc-mem.gcTimeMillis' in name:
        return '_gc_memory_time'
    else:
        return ''


def get_directory(module=None, operator=None, method=None):
    if method:
        path = '{}\\{}\\{}'.format(module, operator, method)
        return path.replace(':+forced-', '_')
    elif operator:
        return '{}\\{}'.format(module, operator)
    elif module:
        return 'group\\{}'.format(module)
    else:
        return 'complete\\complete'


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
    lg_len = len(lg)
    size = 1 if lg_len == 1 else 4
    chunked = list(chunks(lg, size))

    multiple_lists = chunked
    arrays = [np.array(x) for x in multiple_lists]
    mean_list = [np.mean(k) for k in zip(*arrays)]

    all_nan = True if True in np.isnan(np.array(mean_list)) else False
    if (size == 1 and math.isnan(mean_list[0])) or all_nan:
        return ['N/A']
    elif group.name == 'times':
        return [int(x) for x in mean_list]
    else:
        return mean_list


def chunks(lst, n):
    for i in xrange(0, len(lst), n):
        yield lst[i:i + n]


def map_array_to_mb(array):
    if isinstance(array, pd.Series):
        return array.apply(lambda x: bytes_to(x, 'm') if not isinstance(x, list) else map(lambda x: bytes_to(x, 'm'), x))
    return array.apply(lambda series: map(lambda x: bytes_to(x, 'm'), series))


def bytes_to(bytes, to, bsize=1024):
    a = {'k': 1, 'm': 2, 'g': 3, 't': 4, 'p': 5, 'e': 6}
    r = float(bytes)
    for i in range(a[to]):
        r = r / bsize
    return r


def get_benchmark_type(method):
    if 'gc-mem.total' in method:
        return 'Memory GC-Total'
    elif 'gc-mem.used.after' in method:
        return 'Memory GC-After'
    elif 'gc-mem.gcTimeMillis' in method:
        return 'Memory GC-Time'
    elif 'assessments' in method:
        return 'Learning Curve'
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


def calculate_missing_data(results, operators, penalty=2):
    df = results
    missing_rows = pd.DataFrame()

    for operator in operators:
        series = df[df['benchmark'].str.contains(r'\.{}\.'.format(operator))]

        akka = series[series['benchmark'].str.contains('Akka')]
        if ~akka.empty:
            akka['original'] = True
        rx = series[series['benchmark'].str.contains('RxJava')]
        if ~rx.empty:
            rx['original'] = True
        reactor = series[series['benchmark'].str.contains('Reactor')]
        if ~reactor.empty:
            reactor['original'] = True
        solutions_series = [akka, rx, reactor]

        if len(set(map(len, solutions_series))) > 1:

            most_complete = max(solutions_series, key=len)

            if len(akka) == 0:
                reconstructed = reconstruct_whole('Akka', most_complete)
                reconstructed = calculate_penalty_values('Akka', reconstructed, rx, reactor, penalty)
                missing_rows = missing_rows.append(reconstructed)
            elif len(akka) < len(most_complete):
                reconstructed = reconstruct_partial('Akka', akka, most_complete)
                reconstructed = calculate_penalty_values('Akka', reconstructed, rx, reactor, penalty)
                missing_rows = missing_rows.append(reconstructed)

            if len(rx) == 0:
                reconstructed = reconstruct_whole('RxJava', most_complete)
                reconstructed = calculate_penalty_values('RxJava', reconstructed, akka, reactor, penalty)
                missing_rows = missing_rows.append(reconstructed, ignore_index=True)
            elif len(rx) < len(most_complete):
                reconstructed = reconstruct_partial('RxJava', rx, most_complete)
                reconstructed = calculate_penalty_values('RxJava', reconstructed, akka, reactor, penalty)
                missing_rows = missing_rows.append(reconstructed, ignore_index=True)

            if len(reactor) == 0:
                reconstructed = reconstruct_whole('Reactor', most_complete)
                reconstructed = calculate_penalty_values('Reactor', reconstructed, rx, akka, penalty)
                missing_rows = missing_rows.append(reconstructed, ignore_index=True)
            elif len(reactor) < len(most_complete):
                reconstructed = reconstruct_partial('Reactor', reactor, most_complete)
                reconstructed = calculate_penalty_values('Reactor', reconstructed, rx, akka, penalty)
                missing_rows = missing_rows.append(reconstructed, ignore_index=True)

    missing_rows = missing_rows.drop('original', 1)
    return missing_rows


def calculate_penalty_values(solution, reconstructed, based_on, based_on2, penalty):
    for index, row in reconstructed.iterrows():
        param = row['times']
        name = row['benchmark'].split(solution)[-1]
        based_on_no_na = based_on
        based_on2_no_na = based_on2
        if np.isnan(param):
            param = -1
            based_on_no_na = based_on.fillna(-1)
            based_on2_no_na = based_on2.fillna(-1)

        from_1 = based_on_no_na[(based_on_no_na.benchmark.str.contains(name, regex=False)) & (based_on_no_na.times == param) & (
                    based_on_no_na.original == True)]
        from_2 = based_on2_no_na[(based_on2_no_na.benchmark.str.contains(name, regex=False)) & (based_on2_no_na.times == param) & (
                    based_on2_no_na.original == True)]

        if not from_1.empty and not from_2.empty:
            error_1 = from_1['error'].tolist()[0]
            error_2 = from_2['score'].tolist()[0]
            error = max(error_1, error_2) * penalty

            score_1 = from_1['error'].tolist()[0]
            score_2 = from_2['score'].tolist()[0]
            score = max(score_1, score_2) * penalty
        elif not from_1.empty:
            error_1 = from_1['error'].tolist()[0]
            error = error_1 * penalty

            score_1 = from_1['error'].tolist()[0]
            score = score_1 * penalty
        elif not from_2.empty:
            error_2 = from_2['error'].tolist()[0]
            error = error_2 * penalty

            score_2 = from_2['error'].tolist()[0]
            score = score_2 * penalty
        else:
            continue

        reconstructed.loc[index, 'times'] = row['times']
        reconstructed.loc[index, 'score'] = score
        reconstructed.loc[index, 'error'] = error
    return reconstructed


def reconstruct_whole(solution, based_on):
    reconstructed = based_on.copy()
    reconstructed['original'] = False
    reconstructed['benchmark'] = reconstructed['benchmark'].str.replace('Akka|Reactor|RxJava', solution, regex=True)
    reconstructed['score'] = -1.0
    reconstructed['error'] = -1.0
    return reconstructed


def reconstruct_partial(solution, incomplete, based_on):
    reconstructed = pd.DataFrame()
    for index, row in based_on.iterrows():
        param = row['times']
        benchmark = re.sub(r'Akka|Reactor|RxJava', solution, row['benchmark'])

        already_there = incomplete[(incomplete.benchmark == benchmark) & (incomplete.times == param)]
        if len(already_there) == 0:
            copy = row.copy()
            copy['original'] = False
            copy['benchmark'] = re.sub(r'Akka|Reactor|RxJava', solution, row['benchmark'])
            copy['score'] = -1.0
            copy['error'] = -1.0
            reconstructed = reconstructed.append(copy.to_frame().T)

    return reconstructed
