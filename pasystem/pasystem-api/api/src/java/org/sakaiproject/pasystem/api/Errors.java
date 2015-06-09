package org.sakaiproject.pasystem.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import lombok.Value;
import java.util.stream.Collectors;


public class Errors {

    @Value
    static class Error {
        private String field;
        private String errorCode;
    }

    private List<Error> errors;

    public Errors() {
        errors = new ArrayList<Error>();
    }

    public void addError(String field, String code) {
        errors.add(new Error(field, code));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Errors merge(Errors other) {
        errors.addAll(other.toList());
        return this;
    }

    public List<Error> toList() {
        return new ArrayList(errors);
    }

    public Map<String, String> toMap() {
        return errors.stream().collect(Collectors.toMap(Error::getField, Error::getErrorCode));
    }

}
