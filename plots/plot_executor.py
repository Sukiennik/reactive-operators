from utils import *
import numbers


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


def plot_sync_vs_reactive():
    df = pd.DataFrame({
        'x': range(1, 11),
        'mean': np.random.randn(10),
        '50th pct': np.random.randn(10) + range(1, 11),
        '75th pct': np.random.randn(10) + range(11, 21),
        '95th pct': np.random.randn(10) + range(6, 16),
        '99th pct': np.random.randn(10) + range(4, 14) + (0, 0, 0, 0, 0, 0, 0, -3, -8, -6)})

    df2 = pd.DataFrame({
        'x': range(1, 11),
        'mean': np.random.randn(10),
        '50th pct': np.random.randn(10) + range(1, 11),
        '75th pct': np.random.randn(10) + range(11, 21),
        '95th pct': np.random.randn(10) + range(6, 16),
        '99th pct': np.random.randn(10) + range(4, 14) + (0, 0, 0, 0, 0, 0, 0, -3, -8, -6)})


    plt.figure(figsize=(10.5, 10.5))
    plt.subplots_adjust(wspace=0.35, hspace=0.35)

    num = 0
    for column in df.drop('x', axis=1):
        num += 1

        plt.subplot(3, 2, num)

        plt.plot(df['x'], df[column], marker='s', color='red', linewidth=1.9, alpha=0.9, label=column)
        plt.plot(df['x'], df[column], marker='s', color='blue', linewidth=1.9, alpha=0.9, label=column)

        plt.xlim(0, 10)
        plt.ylim(-2, 22)

        plt.legend(['reactive', 'synchronous'])
        plt.title(column, loc='left', fontsize=12, fontweight=0)
        plt.xlabel('Concurrent users')
        plt.ylabel('Response time (s)', rotation=90)

    # general title
    plt.suptitle('Synchronous vs Reactive', fontsize=13, y=0.95)

    plt.savefig('web_sync_vs_reactive.png', bbox_inches='tight')
