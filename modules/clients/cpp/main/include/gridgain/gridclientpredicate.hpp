/* @cpp.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

#ifndef GRIDCLIENTPREDICATE_HPP_INCLUDED
#define GRIDCLIENTPREDICATE_HPP_INCLUDED

/**
 * Defines predicate construct. Predicate like closure is a first-class function
 * that is defined with (or closed over) its free variables that are bound to the closure
 * scope at execution.
 * <h2 class="header">Thread Safety</h2>
 * Note that this interface does not impose or assume any specific thread-safety by its
 * implementations. Each implementation can elect what type of thread-safety it provides,
 * if any.
 * @param <E1> Type of the free variable, i.e. the element the predicate is called on.
 */
template <class E1> class GridClientPredicate {
public:
    /**
     * Destructor.
     *
     */
    virtual ~GridClientPredicate() {};
    /**
     * Predicate body.
     *
     * @param e Bound free variable, i.e. the element the closure is called or closed on.
     * @return Return value.
     */
    virtual bool apply(const E1& e) const = 0;
};

#endif // GRIDCLIENTPREDICATE_HPP_INCLUDED