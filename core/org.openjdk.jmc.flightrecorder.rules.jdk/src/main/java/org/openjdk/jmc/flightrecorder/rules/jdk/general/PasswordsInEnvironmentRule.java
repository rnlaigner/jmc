/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.flightrecorder.rules.jdk.general;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.owasp.encoder.Encode;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.util.IPreferenceValueProvider;
import org.openjdk.jmc.common.util.TypedPreference;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkTypeIDs;
import org.openjdk.jmc.flightrecorder.rules.IRule;
import org.openjdk.jmc.flightrecorder.rules.Result;
import org.openjdk.jmc.flightrecorder.rules.jdk.messages.internal.Messages;
import org.openjdk.jmc.flightrecorder.rules.util.JfrRuleTopics;
import org.openjdk.jmc.flightrecorder.rules.util.RulesToolkit;
import org.openjdk.jmc.flightrecorder.rules.util.RulesToolkit.EventAvailability;

public class PasswordsInEnvironmentRule implements IRule {
	private static final String PWD_RESULT_ID = "PasswordsInEnvironment"; //$NON-NLS-1$

	private Result getResult(IItemCollection items, IPreferenceValueProvider valueProvider) {
		EventAvailability eventAvailability = RulesToolkit.getEventAvailability(items, JdkTypeIDs.ENVIRONMENT_VARIABLE);
		if (eventAvailability != EventAvailability.AVAILABLE) {
			return RulesToolkit.getEventAvailabilityResult(this, items, eventAvailability,
					JdkTypeIDs.ENVIRONMENT_VARIABLE);
		}

		// FIXME: Should extract set of variable names instead of joined string
		String pwds = RulesToolkit.findMatches(JdkTypeIDs.ENVIRONMENT_VARIABLE, items, JdkAttributes.ENVIRONMENT_KEY,
				PasswordsInArgumentsRule.PASSWORD_MATCH_STRING, true);
		if (pwds != null && pwds.length() > 0) {
			String[] envs = pwds.split(", "); //$NON-NLS-1$
			StringBuffer passwords = new StringBuffer("<ul>"); //$NON-NLS-1$
			for (String env : envs) {
				passwords.append("<li>"); //$NON-NLS-1$
				passwords.append(Encode.forHtml(env));
				passwords.append("</li>"); //$NON-NLS-1$
			}
			passwords.append("</ul>"); //$NON-NLS-1$
			pwds = passwords.toString();
			String message = MessageFormat
					.format(Messages.getString(Messages.PasswordsInEnvironmentRuleFactory_TEXT_INFO_LONG), pwds);
			return new Result(this, 100, Messages.getString(Messages.PasswordsInEnvironmentRuleFactory_TEXT_INFO),
					message);
		}
		return new Result(this, 0, Messages.getString(Messages.PasswordsInEnvironmentRuleFactory_TEXT_OK));
	}

	@Override
	public RunnableFuture<Result> evaluate(final IItemCollection items, final IPreferenceValueProvider valueProvider) {
		FutureTask<Result> evaluationTask = new FutureTask<>(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				return getResult(items, valueProvider);
			}
		});
		return evaluationTask;
	}

	@Override
	public Collection<TypedPreference<?>> getConfigurationAttributes() {
		return Collections.emptyList();
	}

	@Override
	public String getId() {
		return PWD_RESULT_ID;
	}

	@Override
	public String getName() {
		return Messages.getString(Messages.PasswordsInEnvironmentRuleFactory_RULE_NAME);
	}

	@Override
	public String getTopic() {
		return JfrRuleTopics.ENVIRONMENT_VARIABLES_TOPIC;
	}
}
