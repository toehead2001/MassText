package com.dallinc.masstext.models;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;

/**
 * Created by dallin on 1/31/15.
 */
public class Template extends SugarRecord {
    public String title;
    public String body;
    @Ignore
    public ArrayList<String> variables;
    public String variable_string;

    public Template() {
    }

    public Template(String t, String b, ArrayList<String> v) {
        title = t;
        body = b;
        variables = v;
    }

    public void buildArrayListFromString() {
        variables = new ArrayList<String>();
        for(String variable: variable_string.split(", ")) {
            variables.add(variable);
        }
    }

    public void saveVariablesAsJson() {
        if(variables.size() < 1) {
            variable_string = "";
            return;
        }
        variable_string = variables.get(0);
        for(String variable: variables.subList(1, variables.size())) {
            variable_string += ", " + variable;
        }
    }

    @Override
    public long save() {
        saveVariablesAsJson();
        return super.save();
    }
}
