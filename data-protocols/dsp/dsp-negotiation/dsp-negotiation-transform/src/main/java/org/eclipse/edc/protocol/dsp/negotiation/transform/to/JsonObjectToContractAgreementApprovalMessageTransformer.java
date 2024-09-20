/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.protocol.dsp.negotiation.transform.to;

import jakarta.json.JsonObject;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreementApprovalMessage;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreementVerificationMessage;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_AGREEMENT_APPROVAL_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;

/**
 * Creates a {@link ContractAgreementVerificationMessage} from a {@link JsonObject}.
 */
public class JsonObjectToContractAgreementApprovalMessageTransformer extends AbstractJsonLdTransformer<JsonObject, ContractAgreementApprovalMessage> {

    public JsonObjectToContractAgreementApprovalMessageTransformer() {
        super(JsonObject.class, ContractAgreementApprovalMessage.class);
    }

    @Override
    public @Nullable ContractAgreementApprovalMessage transform(@NotNull JsonObject object, @NotNull TransformerContext context) {
        var builder = ContractAgreementApprovalMessage.Builder.newInstance();
        if (!transformMandatoryString(object.get(DSPACE_PROPERTY_PROCESS_ID), builder::processId, context)) {
            context.problem()
                    .missingProperty()
                    .type(DSPACE_TYPE_CONTRACT_AGREEMENT_APPROVAL_MESSAGE)
                    .property(DSPACE_PROPERTY_PROCESS_ID)
                    .report();
            return null;
        }

        return builder.build();
    }

}
