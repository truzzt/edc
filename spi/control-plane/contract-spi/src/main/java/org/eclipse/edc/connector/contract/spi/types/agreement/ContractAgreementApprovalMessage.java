/*
 *  Copyright (c) 2021 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.connector.contract.spi.types.agreement;

import org.eclipse.edc.connector.contract.spi.types.protocol.ContractRemoteMessage;
import org.eclipse.edc.policy.model.Policy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.UUID.randomUUID;

public class ContractAgreementApprovalMessage implements ContractRemoteMessage {

    private String id;
    private String processId;

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public void setProtocol(String protocol) {
    }

    @Override
    public String getCounterPartyAddress() {
        return null;
    }

    @Override
    @NotNull
    public String getProcessId() {
        return processId;
    }

    @Override
    public Policy getPolicy() {
        return null;
    }

    public static class Builder {
        private final ContractAgreementApprovalMessage message;

        private Builder() {
            message = new ContractAgreementApprovalMessage();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(String id) {
            message.id = id;
            return this;
        }

        public Builder processId(String processId) {
            message.processId = processId;
            return this;
        }

        public ContractAgreementApprovalMessage build() {
            if (message.id == null) {
                message.id = randomUUID().toString();
            }

            Objects.requireNonNull(message.processId, "processId");
            return message;
        }
    }
}
