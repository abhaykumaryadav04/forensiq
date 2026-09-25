package com.forensiq.identity_scanning.varification.engine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.verifirer.AuthenticityVarifier;

@Service
public class AuthenticityVerificationEngine {
    @Autowired
  private List<AuthenticityVarifier> verifiers;
  public List<VarificationResult> varify(Document document){
     List<VarificationResult> results=new ArrayList<>();
       for(AuthenticityVarifier v:verifiers){
    try { 
  
        VarificationResult r=v.varify(document);
        if(r!=null)  results.add(r);   
    
} catch (Exception exception) {

            results.add(
                    VarificationResult.builder()
                            .varifierName( v.getVarifierName())
                            .varificationType(v.getVerificationType())
                            .varificationStatus( VarificationStatus.NOT_AVAILABLE)
                            .confidence(0.0)
                            .riskScore(0.0)
                            .message(
                                    "Verification could not be completed: "
                                    + exception.getMessage()
                            )
                            .build()
            );
        }
    }
   return results;
  }
}
