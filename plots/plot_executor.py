import pandas as pd

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
                method_group = flat_operator_group.loc[flat_operator_group['method'] == method]

                if plot_single_one is False:
                    if test_plot:
                        plot_single_one = True

                    plot_single(method_group, module, operator, solutions)


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
                if test_plot: plot_single_one = True

                plot_single(flat_operator_group, module, operator, solutions)


def plot_module_level():
    return


def plot_complete_level():
    return


def plot_single(data, module, operator, solutions):
    params_names = data[['times']].iloc[0][0]
    unit = data['unit'].values[0]
    if unit == 'bytes':
        unit = 'MBytes'
        data[['score']] = data[['score']].apply(lambda x: map_array_to_mb(x))
        data[['error']] = data[['error']].apply(lambda x: map_array_to_mb(x))

    results = data[['score']].unstack().apply(pd.Series)
    errors = data[['error']].unstack().apply(pd.Series).rename(index={'error': 'score'})

    fig, ax = plt.subplots()
    results.plot.bar(ax=ax, yerr=errors, rot=0, cmap='RdBu', fontsize=8, width=0.5, figsize=(6.5, 5),
                     capsize=2)
    is_plot_log = should_plot_log(results)

    ax.set_xlabel('Solution')
    ax.set_ylabel(unit, rotation=90)
    ax.legend(params_names, title="Param: times", loc=1, fontsize='small')
    ax.set_xticklabels(solutions)
    ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}, $\bfOperator$: {}".format(
        get_benchmark_type('performance'), module, operator), fontsize=8)
    is_plot_log and plt.yscale('log')

    name = 'plots\\' + get_directory(module, operator)
    save_fig(fig, name)
    plt.yscale('linear')
