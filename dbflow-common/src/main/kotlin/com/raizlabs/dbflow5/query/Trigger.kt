package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.appendOptional
import com.raizlabs.dbflow5.appendQuotedIfNeeded
import com.raizlabs.dbflow5.query.property.IProperty
import com.raizlabs.dbflow5.sql.Query
import kotlin.reflect.KClass

expect class Trigger(name: String) : InternalTrigger {

    companion object {

        @JvmStatic
        fun create(triggerName: String): Trigger
    }
}

/**
 * Description: Describes an easy way to create a SQLite TRIGGER
 */
abstract class InternalTrigger
/**
 * Creates a trigger with the specified trigger name. You need to complete
 * the trigger using
 *
 * @param name What we should call this trigger
 */
internal constructor(
    /**
     * The name in the DB
     */
    /**
     * @return The name of this TRIGGER
     */
    val name: String) : Query {

    /**
     * If it's [.BEFORE], [.AFTER], or [.INSTEAD_OF]
     */
    private var beforeOrAfter: String = ""

    private var temporary: Boolean = false

    override val query: String
        get() {
            val queryBuilder = StringBuilder("CREATE ")
            if (temporary) {
                queryBuilder.append("TEMP ")
            }
            queryBuilder.append("TRIGGER IF NOT EXISTS ")
                .appendQuotedIfNeeded(name).append(" ")
                .appendOptional("$beforeOrAfter ")

            return queryBuilder.toString()
        }

    /**
     * Sets the trigger as temporary.
     */
    fun temporary() = applyThis {
        this.temporary = true
    }

    /**
     * Specifies AFTER eventName
     */
    fun after() = applyThis {
        beforeOrAfter = AFTER
    }

    /**
     * Specifies BEFORE eventName
     */
    fun before() = applyThis {
        beforeOrAfter = BEFORE
    }

    /**
     * Specifies INSTEAD OF eventName
     */
    fun insteadOf() = applyThis {
        beforeOrAfter = INSTEAD_OF
    }

    /**
     * Starts a DELETE ON command
     *
     * @param onTable The table ON
     */
    infix fun <TModel : Any> deleteOn(onTable: KClass<TModel>): TriggerMethod<TModel> =
        TriggerMethod(this as Trigger, TriggerMethod.DELETE, onTable)

    /**
     * Starts a INSERT ON command
     *
     * @param onTable The table ON
     */
    infix fun <TModel : Any> insertOn(onTable: KClass<TModel>): TriggerMethod<TModel> =
        TriggerMethod(this as Trigger, TriggerMethod.INSERT, onTable)

    /**
     * Starts an UPDATE ON command
     *
     * @param onTable    The table ON
     * @param properties if empty, will not execute an OF command. If you specify columns,
     * the UPDATE OF column1, column2,... will be used.
     */
    fun <TModel : Any> updateOn(onTable: KClass<TModel>, vararg properties: IProperty<*>): TriggerMethod<TModel> =
        TriggerMethod(this as Trigger, TriggerMethod.UPDATE, onTable, *properties)

    infix fun <T : Any> updateOn(onTable: KClass<T>): TriggerMethod<T> =
        TriggerMethod(this as Trigger, TriggerMethod.UPDATE, onTable)

    private fun applyThis(fn: InternalTrigger.() -> Unit): Trigger = apply(fn) as Trigger

    companion object {

        /**
         * Specifies that we should do this TRIGGER before some event
         */
        const val BEFORE = "BEFORE"

        /**
         * Specifies that we should do this TRIGGER after some event
         */
        const val AFTER = "AFTER"

        /**
         * Specifies that we should do this TRIGGER instead of the specified events
         */
        const val INSTEAD_OF = "INSTEAD OF"

        /**
         * @param triggerName The name of the trigger to use.
         * @return A new trigger.
         */
        @JvmStatic
        fun create(triggerName: String) = Trigger(triggerName)
    }
}
