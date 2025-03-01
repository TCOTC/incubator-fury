/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fury.util.unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

// CHECKSTYLE.OFF:TypeName
class _Lookup {
  // CHECKSTYLE.ON:TypeName
  static final Lookup IMPL_LOOKUP;
  static volatile MethodHandle CONSTRUCTOR_LOOKUP;
  static volatile boolean CONSTRUCTOR_LOOKUP_ERROR;

  // inspired by fastjson to get trusted lookup for create lambdas function to
  // avoid cost of reflection.
  static {
    Lookup trustedLookup = null;
    {
      try {
        Field implLookup = Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long fieldOffset = _JDKAccess.UNSAFE.staticFieldOffset(implLookup);
        trustedLookup = (Lookup) _JDKAccess.UNSAFE.getObject(Lookup.class, fieldOffset);
      } catch (Throwable ignored) {
        // ignored
      }
      if (trustedLookup == null) {
        trustedLookup = MethodHandles.lookup();
      }
      IMPL_LOOKUP = trustedLookup;
    }
  }

  // CHECKSTYLE.OFF:MethodName
  public static Lookup _trustedLookup(Class<?> objectClass) {
    // CHECKSTYLE.OFF:MethodName
    if (!CONSTRUCTOR_LOOKUP_ERROR) {
      try {
        int trusted = -1;
        MethodHandle constructor = CONSTRUCTOR_LOOKUP;
        if (_JDKAccess.JAVA_VERSION < 14) {
          if (constructor == null) {
            constructor =
                IMPL_LOOKUP.findConstructor(
                    Lookup.class, MethodType.methodType(void.class, Class.class, int.class));
            CONSTRUCTOR_LOOKUP = constructor;
          }
          int fullAccessMask = 31; // for IBM Open J9 JDK
          return (Lookup)
              constructor.invoke(objectClass, _JDKAccess.OPEN_J9 ? fullAccessMask : trusted);
        } else {
          if (constructor == null) {
            constructor =
                IMPL_LOOKUP.findConstructor(
                    Lookup.class,
                    MethodType.methodType(void.class, Class.class, Class.class, int.class));
            CONSTRUCTOR_LOOKUP = constructor;
          }
          return (Lookup) constructor.invoke(objectClass, null, trusted);
        }
      } catch (Throwable ignored) {
        CONSTRUCTOR_LOOKUP_ERROR = true;
      }
    }
    if (_JDKAccess.JAVA_VERSION < 11) {
      Lookup lookup = getLookupByReflection(objectClass);
      if (lookup != null) {
        return lookup;
      }
    }
    return IMPL_LOOKUP.in(objectClass);
  }

  private static MethodHandles.Lookup getLookupByReflection(Class<?> cls) {
    try {
      Constructor<Lookup> constructor =
          MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
      constructor.setAccessible(true);
      return constructor.newInstance(
          cls, -1 // Lookup.TRUSTED
          );
    } catch (Exception e) {
      return null;
    }
  }
}
