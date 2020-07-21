from utils import *
import pandas as pd


def plot_method_level(method_level_grouped, test_plot=False):

    modules = method_level_grouped.index.unique(level='module').values
    for module in modules:
        module_group = method_level_grouped.loc[module]
        operators = module_group.index.unique(level='operator').values

        for operator in operators:
            operator_group = module_group.loc[operator]
            methods = operator_group.index.unique(level='method').values
            flat_operator_group = operator_group.reset_index()

            print flat_operator_group

            for method in methods:
                method_group = flat_operator_group.loc[flat_operator_group['method'] == method]

                if test_plot is False:
                    test_plot = True

                    params_names = method_group[['times']].iloc[0][0]
                    solutions_names = method_group['solution'].values
                    unit = method_group['unit'].values[0]
                    if unit == 'bytes':
                        unit = 'MBytes'
                        method_group[['score']] = method_group[['score']].apply(lambda x: map_array_to_mb(x))
                        method_group[['error']] = method_group[['error']].apply(lambda x: map_array_to_mb(x))

                    results = method_group[['score']].unstack().apply(pd.Series)
                    errors = method_group[['error']].unstack().apply(pd.Series).rename(index={'error': 'score'})

                    fig, ax = plt.subplots()
                    results.plot.bar(ax=ax, yerr=errors, rot=0, cmap='RdBu', fontsize=8, width=0.5, figsize=(6.5, 5),
                                     capsize=2)
                    is_plot_log = should_plot_log(results)

                    ax.set_xlabel('Solution')
                    ax.set_ylabel(unit, rotation=90)
                    ax.legend(params_names, title="Param: times", loc=1, fontsize='small')
                    ax.set_xticklabels(solutions_names)
                    ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}, $\bfOperator$: {}, $\bfMethod$: {}".format(
                        get_benchmark_type(method), module, operator, get_benchmark_method_type(method)), fontsize=8)
                    is_plot_log and plt.yscale('log')

                    name = 'plots\\' + get_directory(module, operator, method)
                    save_fig(fig, name)
                    plt.yscale('linear')


def plot_operator_level(operator_level_grouped, test_plot=False):
    modules = operator_level_grouped.index.unique(level='module').values
    for module in modules:
        module_group = operator_level_grouped.loc[module]
        operators = module_group.index.unique(level='operator').values

        for operator in operators:
            operator_group = module_group.loc[operator]
            solutions = operator_group.index.unique(level='solution').values
            flat_operator_group = operator_group.reset_index()

            print flat_operator_group



def plot_module_level():
    return


def plot_complete_level():
    return
