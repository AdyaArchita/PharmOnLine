package edu.pharmacy.model;

/** A model that can be matched against a simple search phrase. */
public interface Searchable {
    boolean matches(String query);
}
