package eu.kyotoproject.rdf;

import java.util.ArrayList;

/**
 * Created by piek on 9/9/14.
 */
public class SenseFrameRoles {

    /**
     *
     <externalRef confidence="0.165911" reference="nld-21-d_n-36759-n" resource="cdb2.0-nld-all.infv.0.0.no-allwords">
     <externalRef resource="predicate-matrix1.1">
     <externalRef reference="fn:Fluidic_motion" resource="fn"/>
     <externalRef reference="fn-role:Area" resource="fn-role"/>
     <externalRef reference="fn-role:Fluid" resource="fn-role"/>
     <externalRef reference="fn:flow.v" resource="fn"/>
     <externalRef reference="pb:flow.01" resource="pb"/>
     <externalRef reference="fn-role:Goal" resource="fn-role"/>
     <externalRef reference="fn-pb-role:Fluid#1" resource="fn-pb-role"/>
     <externalRef reference="FN_MAPPING;SYNONYMS" resource=""/>
     </externalRef>
     */

    private double confidence;
    private String sense;
    private String resource;
    private String frame;
    private String frameNetLexicalUnit;
    private String propBankLexicalUnit;
    private ArrayList<String> fnRoles;
    private ArrayList<String> pbRoles;
    private ArrayList<String> fnpbRoles;
    private ArrayList<String> roles;

    public SenseFrameRoles() {
        this.confidence = 0;
        this.resource = "";
        this.sense = "";
        this.frame = "";
        this.frameNetLexicalUnit = "";
        this.propBankLexicalUnit = "";
        this.fnRoles = new ArrayList<String>();
        this.pbRoles = new ArrayList<String>();
        this.fnpbRoles = new ArrayList<String>();
        this.roles = new ArrayList<String>();
    }


    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public String getFrameNetLexicalUnit() {
        return frameNetLexicalUnit;
    }

    public void setFrameNetLexicalUnit(String frameNetLexicalUnit) {
        this.frameNetLexicalUnit = frameNetLexicalUnit;
    }

    public String getPropBankLexicalUnit() {
        return propBankLexicalUnit;
    }

    public void setPropBankLexicalUnit(String propBankLexicalUnit) {
        this.propBankLexicalUnit = propBankLexicalUnit;
    }

    public ArrayList<String> getFnRoles() {
        return fnRoles;
    }

    public void setFnRoles(ArrayList<String> fnRoles) {
        this.fnRoles = fnRoles;
    }

    public void addFnRoles(String fnRole) {
        this.fnRoles.add(fnRole);
    }

    public ArrayList<String> getPbRoles() {
        return pbRoles;
    }

    public void setPbRoles(ArrayList<String> pbRoles) {
        this.pbRoles = pbRoles;
    }

    public void addPbRoles(String pbRole) {
        this.pbRoles.add(pbRole);
    }

    public ArrayList<String> getFnpbRoles() {
        return fnpbRoles;
    }

    public void setFnpbRoles(ArrayList<String> fnpbRoles) {
        this.fnpbRoles = fnpbRoles;
    }

    public void addFnpbRoles(String fnpbRole) {
        this.fnpbRoles.add(fnpbRole);
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }

    public void addRoles(String role) {
        this.roles.add(role);
    }

    public String toString () {
        String str = "[\n";
        str += "   frame = \"" + this.getFrame()+"\",\n";
        str += "   sense = \"" + this.getSense()+"\",\n";
        str += "   conf = \"" + this.getConfidence()+"\",\n";
        str += "   roles = \"";

        for (int i = 0; i < roles.size(); i++) {
             str += roles.get(i);
             str += ";";
        }
        str +="\"\n";
        str += "]\n";
        return str;
    }
}
