import numbers

from utils import *


def plot_method_level(method_level_grouped, test_plot=False):
    plot_single_one = False

    modules = method_level_grouped.index.unique(level='module').values
    for module in modules:
        module_group = method_level_grouped.loc[module]
        operators = module_group.index.unique(level='operator').values

        for operator in operators:
            operator_group = module_group.loc[operator]
            methods = operator_group.index.unique(level='method').values
            solutions = operator_group.index.unique(level='solution').values
            flat_operator_group = operator_group.reset_index()

            for method in methods:
                method_group = flat_operator_group.loc[flat_operator_group['method'] == method].reset_index()

                if plot_single_one is False:
                    if test_plot:
                        plot_single_one = True

                    plot_single(method_group, solutions, module, operator, method)


def plot_operator_level(operator_level_grouped, test_plot=False):
    plot_single_one = False

    modules = operator_level_grouped.index.unique(level='module').values
    for module in modules:
        module_group = operator_level_grouped.loc[module]
        operators = module_group.index.unique(level='operator').values

        for operator in operators:
            operator_group = module_group.loc[operator]
            solutions = operator_group.index.unique(level='solution').values
            flat_operator_group = operator_group.reset_index()

            if plot_single_one is False:
                if test_plot:
                    plot_single_one = True

                plot_single(flat_operator_group, solutions, module, operator)


def plot_module_level(module_level_grouped, test_plot=False):
    plot_single_one = False

    modules = module_level_grouped.index.unique(level='module').values
    for module in modules:
        module_group = module_level_grouped.loc[module]
        flat_module_group = module_group.reset_index()
        solutions = module_group.index.unique(level='solution').values

        if plot_single_one is False:
            if test_plot:
                plot_single_one = True

            plot_single(flat_module_group, solutions, module)


def plot_complete_level(solution_level_grouped):
    solutions = solution_level_grouped.index.unique(level='solution').values
    flat_solution_group = solution_level_grouped.reset_index()

    plot_single(flat_solution_group, solutions)


def plot_single(data, solutions, module=None, operator=None, method=None, prefix=''):
    name = data['benchmark'][0]
    params_names = data[['times']].iloc[0][0]
    unit = data['unit'].values[0]
    if unit == 'bytes':
        unit = 'MBytes'
        data[['score']] = data[['score']].apply(lambda x: map_array_to_mb(x)) \
            if not isinstance(data[['score']], numbers.Number) or not isinstance(data[['score']], list) \
            else map_array_to_mb(data[['score']])
        data[['error']] = data[['error']].apply(lambda x: map_array_to_mb(x)) \
            if not isinstance(data[['error']], numbers.Number) or not isinstance(data[['score']], list) \
            else map_array_to_mb(data[['error']])

    results = data[['score']].unstack().apply(pd.Series)
    errors = data[['error']].unstack().apply(pd.Series).rename(index={'error': 'score'})

    fig, ax = plt.subplots()
    # yerr=errors
    results.plot.bar(ax=ax, rot=0, cmap='RdBu', fontsize=8, width=0.5, figsize=(6.5, 5),
                     capsize=2)
    is_plot_log = should_plot_log(results)

    if prefix:
        ax.set_ylim([None, 10])

    ax.set_xlabel('Solution')
    ax.set_ylabel(unit, rotation=90)
    ax.legend(params_names, title="Param: times", loc=4, fontsize='small')
    ax.set_xticklabels(solutions)
    if method:
        ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}, $\bfOperator$: {}, $\bfMethod$: {}".format(
            get_benchmark_type(name), module, operator, get_benchmark_method_type(method)), fontsize=8)
    elif operator:
        ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}, $\bfOperator$: {}".format(
            get_benchmark_type(name), module, operator), fontsize=8)
    elif module:
        ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}".format(
            get_benchmark_type(name), module), fontsize=8)
    else:
        ax.set_title(r"$\bfType$: {}".format(
            get_benchmark_type(name)), fontsize=8)

    is_plot_log and plt.yscale('log')

    name = 'plots\\' + prefix + get_directory(module, operator, method) + get_filename_suffix(name)
    save_fig(fig, name)
    plt.yscale('linear')


def plot_sync_reactive_comparison():
    reactive = pd.DataFrame({
        'x': np.insert(np.arange(start=1000, stop=25001, step=1000), 0, 1),
        '50th percentile': [304, 302, 302, 303, 303, 303, 304, 304, 305, 305, 305, 306, 306, 306, 311, 309, 319, 399,
                            409, 476, 558, 759, 776, 844, 948, 1054],
        '75th percentile': [304, 302, 303, 303, 304, 304, 305, 305, 306, 306, 307, 308, 308, 313, 349, 349, 400, 644,
                            655, 753, 890, 1188, 1162, 1236, 1314, 1443],
        '95th percentile': [305, 303, 304, 306, 309, 308, 310, 314, 317, 319, 334, 348, 354, 392, 503, 543, 821, 1189,
                            1250, 1289, 1459, 1993, 1713, 1833, 1971, 2232],
        '99th percentile': [306, 304, 307, 317, 326, 328, 335, 343, 353, 367, 417, 398, 398, 730, 909, 859, 1144, 1189,
                            1606, 1681, 2011, 2577, 2280, 2384, 2545, 2896],
        'Mean response time': [304, 302, 302, 303, 304, 304, 305, 306, 306, 307, 311, 311, 312, 325, 351, 351, 398, 528,
                               541, 591, 670, 885, 853, 927, 994, 1097],
        'Standard deviation of response time': [1, 1, 1, 4, 4, 4, 5, 7, 8, 13, 29, 18, 18, 73, 107, 103, 175, 298, 314,
                                                350, 416, 629, 575, 745, 795, 844],
        'success': [30, 30000, 60000, 90000, 120000, 150000, 180000, 210000, 240000, 270000, 300000, 330000, 360000,
                    390000, 420000, 450000, 480000, 510000, 540000, 570000, 600000, 630000, 660000, 689273, 719664,
                    749554],
        'failed': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 727, 336, 446],
        'Mean requests/second': [0.5, 340.909, 689.655, 1022.727, 1333.333, 1704.545, 2000, 2359.551, 2727.273,
                                 3068.182, 3370.787, 3750, 4044.944, 4333.333, 4666.667, 5000, 5274.725, 5312.5,
                                 5567.01, 5816.327, 5882.353, 5833.333, 6055.046, 6106.195, 6260.87, 6465.517],
        't<800ms': [30, 30000, 60000, 90000, 120000, 150000, 180000, 210000, 240000, 270000, 299999, 330000, 360000,
                    387718, 411968, 442964, 454327, 427338, 448238, 444566, 412735, 332059, 340215, 324351, 297925,
                    282578],
        '800ms<t<1200ms': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1855, 7826, 6991, 22609, 58015, 60293, 87126, 126994,
                           144250, 170565, 178569, 188676, 160642],
        't>1200ms': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 427, 206, 45, 3064, 24647, 31469, 38308, 60271, 153691,
                     149220, 186353, 233063, 306334]
    })

    sync = pd.DataFrame({
        'x': np.insert(np.arange(start=1000, stop=25001, step=1000), 0, 1),
        '50th percentile': [305, 303, 303, 304, 304, 304, 305, 306, 307, 307, 309, 362, 323, 333, 380, 588, 540, 604,
                            692, 693, 765, 786, 906, 1002, 851, 437],
        '75th percentile': [305, 304, 304, 304, 305, 306, 307, 308, 312, 310, 319, 545, 393, 444, 584, 928, 976, 1066,
                            1246, 1321, 1458, 1443, 1647, 1684, 1645, 1356],
        '95th percentile': [307, 304, 305, 307, 308, 310, 321, 318, 359, 344, 433, 1447, 801, 1157, 1349, 1889, 1865,
                            2257, 2608, 2910, 3174, 2753, 3727, 3116, 3883, 3096],
        '99th percentile': [308, 306, 308, 311, 319, 333, 444, 352, 529, 406, 637, 3862, 1242, 2033, 2377, 4084, 4026,
                            5015, 5667, 7650, 7463, 5623, 8974, 7576, 15045, 6355],
        'Mean response time': [305, 303, 303, 304, 305, 306, 310, 308, 317, 313, 329, 570, 425, 495, 570, 826, 829, 927,
                               1119, 1206, 1291, 1267, 1602, 1513, 1709, 911],
        'Standard deviation of response time': [1, 1, 2, 2, 4, 5, 25, 8, 38, 22, 74, 879, 687, 867, 961, 1300, 1536,
                                                1679, 1691, 2005, 2160, 2161, 2873, 2594, 3181, 1617],
        'success': [30, 30000, 60000, 90000, 120000, 150000, 180000, 210000, 240000, 270000, 300000, 327544, 355057,
                    381871, 407605, 430726, 456865, 480368, 503183, 527689, 548359, 572689, 579570, 603799, 610682,
                    444115],
        'failed': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2456, 4943, 8129, 12395, 19274, 23135, 29632, 36817, 42311, 51641,
                   57311, 80430, 86201, 109318, 305885],
        'Mean requests/second': [0.536, 344.828, 674.157, 1022.727, 1379.31, 1704.545, 2022.472, 2359.551, 2696.629,
                                 3000, 2752.294, 2752.471, 2903.226, 3362.069, 3529.412, 3435.115, 3428.571, 3566.434,
                                 3648.649, 3904.11, 3846.154, 4064.516, 3728.814, 4181.818, 3956.044, 1744.186],
        't<800ms': [30, 30000, 60000, 90000, 120000, 150000, 180000, 210000, 240000, 270000, 300000, 290719, 336933,
                    347291, 350129, 289367, 297929, 285202, 265535, 277244, 258954, 261963, 218679, 207322, 232994,
                    154512],
        '800ms<t<1200ms': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17996, 14176, 15968, 32377, 68367, 74780, 89544, 93764,
                           89676, 92918, 98262, 105100, 95573, 111047, 76927],
        't>1200ms': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18829, 3948, 18612, 25099, 72992, 84156, 105622, 143884, 160769,
                     196487, 212464, 255791, 300904, 266641, 213306]

    })

    plt.figure(figsize=(12.5, 12.5))
    plt.subplots_adjust(wspace=0.35, hspace=0.5)

    num = 0
    for column in reactive.drop(['x', 'success', 'failed', 't<800ms', '800ms<t<1200ms', 't>1200ms'], axis=1):
        num += 1

        plt.subplot(5, 2, num)
        plt.plot(reactive['x'], reactive[column], marker='s', markersize=3, color='red', linewidth=1.9, alpha=0.9,
                 label=column)
        plt.plot(sync['x'], sync[column], marker='s', markersize=3, color='blue', linewidth=1.9, alpha=0.9,
                 label=column)

        # plt.yscale('log')
        plt.legend(['reactive', 'synchronous'])
        plt.title(column, loc='left', fontsize=12, fontweight=0)
        # plt.xticks(reactive2['x'])
        plt.xlim(1, 25000)
        plt.xlabel('Concurrent users')
        # plt.xscale('log')
        plt.ylabel('Response time (ms)', rotation=90)
        if column == 'Mean requests/second':
            plt.ylabel('Mean requests/second', rotation=90)

    # plt.subplot(5, 2, 8)
    # plt.stackplot(reactive['x'], [reactive['failed'], reactive['success']],
    #               labels=['failed requests', 'success requests'], colors=['#F1948A', '#7DCEA0'], alpha=0.4)
    # plt.title('Reactive OK/KO requests', loc='left', fontsize=12, fontweight=0)
    # # plt.yscale('log')
    # plt.legend(loc='upper right')
    # # plt.xticks(reactive2['x'])
    # plt.xlim(500, 23000)
    # plt.xlabel('Concurrent users')
    # plt.ylabel('Requests count', rotation=90)

    plt.subplot(5, 2, 8)
    plt.stackplot(sync['x'], [sync['failed'], sync['success']],
                  labels=['failed requests', 'success requests'], colors=['red', '#7DCEA0'], alpha=0.4)
    plt.title('Synchronous OK/KO requests', loc='left', fontsize=12, fontweight=0)
    # plt.yscale('log')
    plt.legend(loc='upper right')
    # plt.xticks(reactive2['x'])
    plt.yticks([30000, 150000, 270000, 390000, 510000, 630000, 750000])
    plt.xlim(1, 25000)
    plt.xlabel('Concurrent users')
    plt.ylabel('Requests count', rotation=90)

    plt.subplot(5, 2, 9)
    totals = [i + j + k + l for i, j, k, l in
              zip(sync['t<800ms'], sync['800ms<t<1200ms'], sync['t>1200ms'], sync['failed'])]
    t_less800 = [i / float(j) * 100 for i, j in zip(sync['t<800ms'], totals)]
    t_between800and1200 = [i / float(j) * 100 for i, j in zip(sync['800ms<t<1200ms'], totals)]
    t_greater1200 = [i / float(j) * 100 for i, j in zip(sync['t>1200ms'], totals)]
    t_failed = [i / float(j) * 100 for i, j in zip(sync['failed'], totals)]
    bar_width = 0.85

    indicators = np.arange(0, 26)
    plt.bar(indicators, t_less800, color='green', edgecolor='white', width=bar_width, alpha=0.4)
    plt.bar(indicators, t_between800and1200, bottom=t_less800, color='yellow', edgecolor='white', width=bar_width,
            alpha=0.4)
    plt.bar(indicators, t_greater1200, bottom=[i + j for i, j in zip(t_less800, t_between800and1200)], color='orange',
            edgecolor='white', width=bar_width, alpha=0.4)
    plt.bar(indicators, t_failed, bottom=[i + j + k for i, j, k in zip(t_less800, t_between800and1200, t_greater1200)],
            color='red', edgecolor='white', width=bar_width, alpha=0.4)

    plt.title('Synchronous % time response distribution', loc='left', fontsize=12, fontweight=0)
    plt.legend(['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'])
    plt.xticks(indicators, sync['x'], rotation=90, fontsize=8)
    plt.xlabel('Concurrent users', y=0.7)
    plt.ylabel('Percentage', rotation=90)

    plt.subplot(5, 2, 10)
    totals = [i + j + k + l for i, j, k, l in
              zip(reactive['t<800ms'], reactive['800ms<t<1200ms'], reactive['t>1200ms'], reactive['failed'])]
    t_less800 = [i / float(j) * 100 for i, j in zip(reactive['t<800ms'], totals)]
    t_between800and1200 = [i / float(j) * 100 for i, j in zip(reactive['800ms<t<1200ms'], totals)]
    t_greater1200 = [i / float(j) * 100 for i, j in zip(reactive['t>1200ms'], totals)]
    t_failed = [i / float(j) * 100 for i, j in zip(reactive['failed'], totals)]

    bar_width = 0.85
    indicators = np.arange(0, 26)
    plt.bar(indicators, t_less800, color='green', edgecolor='white', width=bar_width, alpha=0.4)
    plt.bar(indicators, t_between800and1200, bottom=t_less800, color='yellow', edgecolor='white', width=bar_width,
            alpha=0.4)
    plt.bar(indicators, t_greater1200, bottom=[i + j for i, j in zip(t_less800, t_between800and1200)], color='orange',
            edgecolor='white', width=bar_width, alpha=0.4)
    plt.bar(indicators, t_failed, bottom=[i + j + k for i, j, k in zip(t_less800, t_between800and1200, t_greater1200)],
            color='red', edgecolor='white', width=bar_width, alpha=0.4)

    plt.title('Reactive % time response distribution', loc='left', fontsize=12, fontweight=0)
    plt.legend(['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'])
    plt.xticks(indicators, reactive['x'], rotation=90, fontsize=8)
    plt.xlabel('Concurrent users', y=0.7)
    plt.ylabel('Percentage', rotation=90)

    plt.suptitle('Synchronous and reactive response time comparison', fontsize=13, y=0.93)

    plt.savefig('plots/web/web_sync_vs_reactive.png', bbox_inches='tight')


def plot_reactive_solutions():
    reactor_1k = pd.DataFrame({
        'x': [3, 750, 1500, 2250, 3000],
        '50th percentile': [307, 305, 305, 306, 312],
        '75th percentile': [308, 307, 307, 309, 323],
        '95th percentile': [310, 309, 312, 316, 357],
        '99th percentile': [310, 313, 318, 327, 392],
        'Mean response time': [306, 305, 306, 307, 318],
        'Standard deviation of response time': [2, 3, 4, 5, 19],
        'success': [90, 22500, 45000, 67500, 90000],
        'failed': [0, 0, 0, 0, 0],
        'Mean requests/second': [1.607, 255.682, 511.364, 767.045, 1022.727],
        't<800ms': [90, 22500, 45000, 67500, 90000],
        '800ms<t<1200ms': [0, 0, 0, 0, 0],
        't>1200ms': [0, 0, 0, 0, 0]
    })
    reactor_5k = pd.DataFrame({
        '50th percentile': [318, 318, 775, 948, 1971],
        '75th percentile': [322, 324, 1534, 1471, 3410],
        '95th percentile': [324, 339, 2783, 2291, 5815],
        '99th percentile': [327, 356, 3198, 2954, 6867],
        'Mean response time': [315, 318, 1078, 1067, 2417],
        'Standard deviation of response time': [8, 12, 817, 652, 1716],
        'success': [90, 22500, 45000, 67500, 90000],
        'failed': [0, 0, 0, 0, 0],
        'Mean requests/second': [1.579, 258.621, 384.615, 597.345, 542.169],
        't<800ms': [90, 22500, 22951, 27904, 15518],
        '800ms<t<1200ms': [0, 0, 6829, 14985, 11250],
        't>1200ms': [0, 0, 15220, 24611, 63232],
    })
    reactor_10k = pd.DataFrame({
        '50th percentile': [328, 353, 1076, 2920, 4884],
        '75th percentile': [336, 406, 1990, 4613, 9807],
        '95th percentile': [341, 612, 4354, 6116, 15059],
        '99th percentile': [347, 872, 6115, 7074, 17724],
        'Mean response time': [323, 390, 1524, 3152, 6404],
        'Standard deviation of response time': [15, 113, 1298, 1783, 4590],
        'success': [90, 22500, 45000, 67500, 90000],
        'failed': [0, 0, 0, 0, 0],
        'Mean requests/second': [1.525, 252.809, 314.685, 342.64, 271.903],
        't<800ms': [90, 22155, 16558, 5803, 3893],
        '800ms<t<1200ms': [0, 312, 8191, 3227, 2537],
        't>1200ms': [0, 33, 20251, 58470, 83570],
    })
    reactor_25k = pd.DataFrame({
        '50th percentile': [356, 1507, 2436, 22572, 426],
        '75th percentile': [377, 3090, 8735, 29784, 3381],
        '95th percentile': [382, 5087, 24770, 36702, 17108],
        '99th percentile': [386, 7796, 27815, 41534, 39785],
        'Mean response time': [346, 2062, 6234, 19063, 3292],
        'Standard deviation of response time': [32, 1672, 7950, 12617, 7221],
        'success': [90, 22500, 45000, 67500, 67282],
        'failed': [0, 0, 0, 0, 22718],
        'Mean requests/second': [1.607, 138.037, 118.11, 76.271, 68.389],
        't<800ms': [90, 6602, 8582, 908, 28633],
        '800ms<t<1200ms': [0, 3469, 4730, 527, 2793],
        't>1200ms': [0, 12429, 31688, 66065, 35856],
    })

    reactorAggregated = pd.DataFrame({
        '50th percentile': [[307, 305, 305, 306, 312], [318, 318, 775, 948, 1971], [328, 353, 1076, 2920, 4884],
                            [356, 1507, 2436, 22572, 426]],
        '75th percentile': [[308, 307, 307, 309, 323], [322, 324, 1534, 1471, 3410], [336, 406, 1990, 4613, 9807],
                            [377, 3090, 8735, 29784, 3381]],
        '95th percentile': [[310, 309, 312, 316, 357], [324, 339, 2783, 2291, 5815], [341, 612, 4354, 6116, 15059],
                            [382, 5087, 24770, 36702, 17108]],
        '99th percentile': [[310, 313, 318, 327, 392], [327, 356, 3198, 2954, 6867], [347, 872, 6115, 7074, 17724],
                            [386, 7796, 27815, 41534, 39785]],
        'Mean response time': [[306, 305, 306, 307, 318], [315, 318, 1078, 1067, 2417], [323, 390, 1524, 3152, 6404],
                               [346, 2062, 6234, 19063, 3292]],
        'Standard deviation of response time': [[2, 3, 4, 5, 19], [8, 12, 817, 652, 1716], [15, 113, 1298, 1783, 4590],
                                                [32, 1672, 7950, 12617, 7221]],
    })

    plt.figure(figsize=(12.5, 12.5))
    plt.subplots_adjust(wspace=0.35, hspace=0.5)

    num = 0
    for column in reactor_1k.drop(['x', 'success', 'failed', 't<800ms', '800ms<t<1200ms', 't>1200ms'], axis=1):
        num += 1

        plt.subplot(4, 2, num)
        plt.plot(reactor_1k['x'], reactor_1k[column], marker='s', markersize=3, color='#B03A2E', linewidth=1,
                 label='1000 size')
        plt.plot(reactor_1k['x'], reactor_5k[column], marker='s', markersize=3, color='#E74C3C', linewidth=1,
                 label='5000 size')
        plt.plot(reactor_1k['x'], reactor_10k[column], marker='s', markersize=3, color='#F1948A', linewidth=1,
                 label='10000 size')
        plt.plot(reactor_1k['x'], reactor_25k[column], marker='s', markersize=3, color='#FADBD8', linewidth=1,
                 label='25000 size')

        a = np.array([reactor_1k[column], reactor_5k[column], reactor_10k[column], reactor_25k[column]])
        mean = np.mean(a, axis=0)
        plt.plot(reactor_1k['x'], mean, marker='s', markersize=3, color='#78281F', linewidth=2, label='mean')
        plt.xticks(reactor_1k['x'])
        plt.legend()
        plt.xlim(1, 3000)

        # plt.yscale('log')
        plt.legend()
        plt.title(column, loc='left', fontsize=12, fontweight=0)
        plt.xlabel('Concurrent users')
        plt.ylabel('Response time (ms)', rotation=90)
        if column == 'Mean requests/second':
            plt.ylabel('Mean requests/second', rotation=90)

    cluster_1k = create_bar_plot_cluster(reactor_1k, ['t<800ms', '800ms<t<1200ms', 't>1200ms', 'failed'],
                                         ['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'],
                                         reactor_1k['x'])
    cluster_5k = create_bar_plot_cluster(reactor_1k, ['t<800ms', '800ms<t<1200ms', 't>1200ms', 'failed'],
                                         ['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'],
                                         reactor_1k['x'])
    cluster_10k = create_bar_plot_cluster(reactor_10k, ['t<800ms', '800ms<t<1200ms', 't>1200ms', 'failed'],
                                          ['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'],
                                          reactor_1k['x'])
    cluster_25k = create_bar_plot_cluster(reactor_25k, ['t<800ms', '800ms<t<1200ms', 't>1200ms', 'failed'],
                                          ['t < 800 ms', '800 ms < t < 1200 ms', 't > 1200 ms', 'failed (KO)'],
                                          reactor_1k['x'])
    axe = plt.subplot(4, 2, 8)
    plot_clustered_stacked(axe, [cluster_1k, cluster_5k, cluster_10k, cluster_25k],
                           ['1000 size', '5000 size', '1000 size', '250000 size'],
                           ['green', 'yellow', 'orange', 'red'], title='Reactive % time response distribution',
                           alpha=0.4, edgecolor='white', width=0.85)
    plt.ylim(0, 100)
    plt.xlabel('Concurrent users', y=0.7)
    plt.ylabel('Percentage', rotation=90)

    plt.suptitle('Reactive solutions response time comparison', fontsize=13, y=0.93)

    plt.savefig('plots/web/web_reactive_solutions.png', bbox_inches='tight')
