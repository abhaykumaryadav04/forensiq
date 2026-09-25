package com.forensiq.identity_scanning.varification.verifirer;

import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationType;

public interface AuthenticityVarifier {
 String getVarifierName();
 VarificationResult varify(Document document);
 VarificationType getVerificationType();
}
