//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.meta;

import cloud.commandframework.Command;
import cloud.commandframework.keys.CloudKey;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Object that is associated with a {@link Command}.
 * Command meta should not be mutable, as one fixed instance will be used per command.
 * <p>
 * Appropriate use for command meta would be fixed state, such as command descriptions.
 */
@API(status = API.Status.STABLE)
public abstract class CommandMeta {

    private static final Key<String> LEGACY_HIDDEN = Key.of(String.class, "hidden");
    public static final Key<String> DESCRIPTION = Key.of(String.class, "description");
    public static final Key<String> LONG_DESCRIPTION = Key.of(String.class, "long-description");

    /**
     * Deprecated, see {@link #HIDDEN_FROM}
     */
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.9.0")
    public static final Key<Boolean> HIDDEN = Key.of(
            Boolean.class,
            "cloud:hidden",
            meta -> Boolean.valueOf(meta.getOrDefault(LEGACY_HIDDEN, "false"))
    );

    /**
     * A set of locations where this command should not be displayed to the user.
     * @since 1.9.0
     */
    public static final Key<Set<Location>> HIDDEN_FROM = Key.of(
            new TypeToken<Set<Location>>() {},
            "cloud:hidden_from",
            meta -> Collections.emptySet()
    );

    /**
     * Create a new simple command meta builder
     *
     * @return Builder instance
     */
    public static SimpleCommandMeta.@NonNull Builder simple() {
        return SimpleCommandMeta.builder();
    }

    @Override
    public final @NonNull String toString() {
        return "";
    }

    /**
     * Get the value associated with a key
     *
     * @param key Key
     * @return Optional that may contain the associated value
     * @deprecated for removal since 1.3.0, see typesafe variant at {@link #get(Key)} instead
     */
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.3.0")
    public abstract @NonNull Optional<String> getValue(@NonNull String key);

    /**
     * Get the value if it exists, else return the default value
     *
     * @param key          Key
     * @param defaultValue Default value
     * @return Value, or default value
     * @deprecated for removal since 1.3.0, see typesafe variant at {@link #getOrDefault(Key, Object)} instead
     */
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.3.0")
    public abstract @NonNull String getOrDefault(@NonNull String key, @NonNull String defaultValue);

    /**
     * Get the value associated with a key.
     *
     * @param <V> Value type
     * @param key Key
     * @return Optional that may contain the associated value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract <V> @NonNull Optional<V> get(@NonNull Key<V> key);

    /**
     * Get the value if it exists, else return the default value.
     *
     * @param <V>          Value type
     * @param key          Key
     * @param defaultValue Default value
     * @return Value, or default value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract <V> @NonNull V getOrDefault(@NonNull Key<V> key, @NonNull V defaultValue);

    /**
     * Get a copy of the meta map
     *
     * @return Copy of meta map
     * @deprecated for removal since 1.3.0, use {@link #getAllValues()} instead.
     */
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.3.0")
    public abstract @NonNull Map<@NonNull String, @NonNull String> getAll();

    /**
     * Get a copy of the meta map, without type information.
     *
     * @return Copy of meta map
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract @NonNull Map<@NonNull String, @NonNull ?> getAllValues();


    /**
     * A key into the metadata map.
     *
     * @param <V> value type
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public interface Key<V> extends CloudKey<V> {

        /**
         * Create a new metadata key.
         *
         * @param type the value type
         * @param key  the name for the key
         * @param <T>  the value type
         * @return a new key
         */
        static <T> @NonNull Key<T> of(final @NonNull Class<T> type, final @NonNull String key) {
            if (GenericTypeReflector.isMissingTypeParameters(type)) {
                throw new IllegalArgumentException("Raw type " + type + " is prohibited");
            }

            return new SimpleKey<>(
                    TypeToken.get(requireNonNull(type, "type")),
                    requireNonNull(key, "key"),
                    null
            );
        }

        /**
         * Create a new metadata key.
         *
         * @param type the value type
         * @param key  the name for the key
         * @param <T>  the value type
         * @return a new key
         */
        static <T> @NonNull Key<T> of(final @NonNull TypeToken<T> type, final @NonNull String key) {
            return new SimpleKey<>(
                    requireNonNull(type, "type"),
                    requireNonNull(key, "key"),
                    null
            );
        }

        /**
         * Create a new metadata key.
         *
         * @param type               the value type
         * @param key                the name for the key
         * @param fallbackDerivation A function that will be called if no value is present for the key
         * @param <T>                the value type
         * @return a new key
         */
        static <T> @NonNull Key<T> of(
                final @NonNull Class<T> type,
                final @NonNull String key,
                final @NonNull Function<@NonNull CommandMeta, @Nullable T> fallbackDerivation
        ) {
            return new SimpleKey<>(
                    TypeToken.get(requireNonNull(type, "type")),
                    requireNonNull(key, "key"),
                    fallbackDerivation
            );
        }

        /**
         * Create a new metadata key.
         *
         * @param type               the value type
         * @param key                the name for the key
         * @param fallbackDerivation A function that will be called if no value is present for the key
         * @param <T>                the value type
         * @return a new key
         */
        static <T> @NonNull Key<T> of(
                final @NonNull TypeToken<T> type,
                final @NonNull String key,
                final @NonNull Function<@NonNull CommandMeta, @Nullable T> fallbackDerivation
        ) {
            return new SimpleKey<>(
                    requireNonNull(type, "type"),
                    requireNonNull(key, "key"),
                    fallbackDerivation
            );
        }

        @Override
        @NonNull
        default TypeToken<@NonNull V> getType() {
            return this.getValueType();
        }

        /**
         * Get a representation of the type of value this key holds.
         *
         * @return the value type
         */
        @NonNull TypeToken<V> getValueType();

        /**
         * Get the name of this key
         *
         * @return the key type
         */
        @Override
        @NonNull String getName();

        /**
         * Get a function that can be used to compute a fallback based on existing meta.
         *
         * <p>This function will only be used if no value is directly for the key.</p>
         *
         * @return the fallback derivation
         */
        @Nullable Function<@NonNull CommandMeta, @Nullable V> getFallbackDerivation();
    }

    /**
     * A place that a command can be displayed or hidden.
     *
     * @since 1.8.0
     */
    public enum Location {
        /**
         * Suggestions while typing the command
         */
        SUGGESTIONS,
        /**
         * In help menus
         */
        HELP
    }
}
