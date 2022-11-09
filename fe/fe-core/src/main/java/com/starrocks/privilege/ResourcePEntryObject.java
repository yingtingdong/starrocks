// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.

package com.starrocks.privilege;

import com.google.gson.annotations.SerializedName;
import com.starrocks.server.GlobalStateMgr;

import java.util.List;
import java.util.Objects;

public class ResourcePEntryObject implements PEntryObject {
    @SerializedName(value = "n")
    String name;  // can be null, means all resources

    public static ResourcePEntryObject generate(GlobalStateMgr mgr, List<String> tokens) throws PrivilegeException {
        if (tokens.size() != 1) {
            throw new PrivilegeException("invalid object tokens, should have one: " + tokens);
        }
        String name = tokens.get(0);
        if (! mgr.getResourceMgr().containsResource(name)) {
            throw new PrivilegeException("cannot find resource: " + tokens.get(0));
        }
        return new ResourcePEntryObject(name);
    }

    public static ResourcePEntryObject generate(
            List<String> allTypes, String restrictType, String restrictName) throws PrivilegeException {
        // only support ON ALL RESOURCES
        if (allTypes.size() != 1 || restrictType != null || restrictName != null) {
            throw new PrivilegeException("invalid ALL statement for resource! only support ON ALL RESOURCES");
        }
        return new ResourcePEntryObject(null);
    }

    protected ResourcePEntryObject(String name) {
        this.name = name;
    }

    /**
     * if the current resource matches other resource, including fuzzy matching.
     *
     * this(hive0), other(hive0) -> true
     * this(hive0), other(ALL) -> true
     * this(ALL), other(hive0) -> false
     */
    @Override
    public boolean match(Object obj) {
        if (!(obj instanceof ResourcePEntryObject)) {
            return false;
        }
        ResourcePEntryObject other = (ResourcePEntryObject) obj;
        if (other.name == null) {
            return true;
        }
        return other.name.equals(name);
    }

    @Override
    public boolean isFuzzyMatching() {
        return name == null;
    }

    @Override
    public boolean validate(GlobalStateMgr globalStateMgr) {
        return globalStateMgr.getResourceMgr().containsResource(name);
    }

    @Override
    public int compareTo(PEntryObject obj) {
        if (!(obj instanceof ResourcePEntryObject)) {
            throw new ClassCastException("cannot cast " + obj.getClass().toString() + " to " + this.getClass());
        }
        ResourcePEntryObject o = (ResourcePEntryObject) obj;
        // other > all
        if (name == null) {
            if (o.name == null) {
                return 0;
            } else {
                return 1;
            }
        }
        if (o.name == null) {
            return -1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public PEntryObject clone() {
        return new ResourcePEntryObject(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourcePEntryObject that = (ResourcePEntryObject) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
