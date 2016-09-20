// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.config.provision;

import java.util.Optional;

/**
 * A capacity request.
 *
 * @author lulf
 * @author bratseth
 */
public final class Capacity {

    private final int nodeCount;

    private final boolean required;

    private final Optional<String> flavor;
    
    private final NodeType type;

    private Capacity(int nodeCount, boolean required, Optional<String> flavor, NodeType type) {
        this.nodeCount = nodeCount;
        this.flavor = flavor;
        this.required = required;
        this.type = type;
    }

    /** Returns the number of nodes requested */
    public int nodeCount() { return nodeCount; }

    /** Returns whether the requested number of nodes must be met exactly for a request for this to succeed */
    public boolean isRequired() { return required; }

    /**
     * The node flavor requested, or empty if no particular flavor is specified.
     * This may be satisfied by the requested flavor or a suitable replacement
     */
    public Optional<String> flavor() { return flavor; }

    /**
     * Returns the node type (role) requested. This is tenant nodes by default.
     * If some other type is requested the node count and flavor may be ignored
     * and all nodes of the requested type returned instead.
     */
    public NodeType type() { return type; }

    @Override
    public String toString() {
        return nodeCount + " nodes " + ( flavor.isPresent() ? "of flavor " + flavor.get() : "(default flavor)" );
    }

    /** Creates this from a desired node count: The request may be satisfied with a smaller number of nodes. */
    public static Capacity fromNodeCount(int capacity) {
        return fromNodeCount(capacity, Optional.empty());
    }
    /** Creates this from a desired node count: The request may be satisfied with a smaller number of nodes. */
    public static Capacity fromNodeCount(int nodeCount, String flavor) {
        return fromNodeCount(nodeCount, Optional.of(flavor));
    }
    /** Creates this from a desired node count: The request may be satisfied with a smaller number of nodes. */
    public static Capacity fromNodeCount(int nodeCount, Optional<String> flavor) {
        return new Capacity(nodeCount, false, flavor, NodeType.tenant);
    }

    /** Creates this from a required node count: Requests must fail unless the node count can be satisfied exactly */
    public static Capacity fromRequiredNodeCount(int nodeCount) {
        return fromRequiredNodeCount(nodeCount, Optional.empty());
    }
    /** Creates this from a required node count: Requests must fail unless the node count can be satisfied exactly */
    public static Capacity fromRequiredNodeCount(int nodeCount, String flavor) {
        return fromRequiredNodeCount(nodeCount, Optional.of(flavor));
    }
    /** Creates this from a required node count: Requests must fail unless the node count can be satisfied exactly */
    public static Capacity fromRequiredNodeCount(int nodeCount, Optional<String> flavor) {
        return new Capacity(nodeCount, true, flavor, NodeType.tenant);
    }
    
    /** Creates this from a node type */
    public static Capacity fromRequiredNodeType(NodeType type) {
        return new Capacity(0, true, Optional.empty(), type);
    }

}
