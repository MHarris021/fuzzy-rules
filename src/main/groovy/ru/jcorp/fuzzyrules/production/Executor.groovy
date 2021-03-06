/*
 * This file is part of Fuzzy Rules.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.jcorp.fuzzyrules.production

import ru.jcorp.fuzzyrules.exceptions.UnresolvedRuleSystemException
import ru.jcorp.fuzzyrules.gui.controls.InputProvider
import ru.jcorp.fuzzyrules.model.Math
import ru.jcorp.fuzzyrules.model.RuleSet

/**
 * @author artamonov
 */
class Executor {

    private ProductionMethod productionMethod
    private RuleSet ruleSet
    private InputProvider inputProvider
    private volatile Algebra algebra

    private Thread executorThread

    Executor(ProductionMethod productionMethod, InputProvider inputProvider, Algebra algebra, RuleSet ruleSet) {
        this.productionMethod = productionMethod
        this.ruleSet = ruleSet
        this.inputProvider = inputProvider
        this.algebra = algebra
    }

    void performProduction() {
        executorThread = new Thread(new Runnable() {
            @Override
            void run() {
                Math.setAlgebra(algebra)

                try {
                    productionMethod.perform(ruleSet)
                } catch (UnresolvedRuleSystemException ignored) {
                    inputProvider.printUnresolvedSystemMessage()
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    inputProvider.showProductionError(e)
                }
            }
        })
        executorThread.start()
    }

    void cancel() {
        if (executorThread.alive)
            executorThread.interrupt()
    }
}