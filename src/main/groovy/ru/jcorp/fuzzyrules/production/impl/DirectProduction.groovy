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

package ru.jcorp.fuzzyrules.production.impl

import ru.jcorp.fuzzyrules.exceptions.*
import ru.jcorp.fuzzyrules.model.*
import ru.jcorp.fuzzyrules.production.*

import static ru.jcorp.fuzzyrules.util.DslSupport.linkClosureToDelegate

/**
 * @author artamonov
 */
class DirectProduction implements ProductionMethod {

    private final DomainObject domainObject

    DirectProduction(DomainObject domainObject) {
        this.domainObject = domainObject
    }

    @Override
    void perform(RuleSet ruleSet) {
        int runCount = 0

        while (!domainObject.resolved &&
                runCount != ruleSet.size) {

            for (Rule rule : ruleSet.rules) {
                boolean conjValue = true
                boolean allValuesResolved = true

                def conjIter = rule.ifStatements.iterator()
                while (conjValue && conjIter.hasNext()) {
                    Closure conj = linkClosureToDelegate(conjIter.next(), domainObject)

                    def conjResult
                    try {
                        conjResult = conj.call()
                    } catch (CannotInputVariableException ignored) {
                        allValuesResolved = false
                        break
                    }
                    // todo Fuzzy &&
                    if (conjResult instanceof Boolean)
                        conjValue = conjResult
                    else if (conjResult != null)
                        throw new RuleStatementException(rule.name)
                }

                if (allValuesResolved && conjValue) {
                    Closure thenClosure = linkClosureToDelegate(rule.thenStatement, domainObject)
                    thenClosure.call()

                    domainObject.addActivatedRule(rule)

                    if (domainObject.resolved) {
                        domainObject.reason = rule.reason
                        break
                    }
                }
            }
            runCount++
        }

        if (!domainObject.resolved)
            throw new UnresolvedRuleSystemException()
    }
}