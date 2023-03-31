/**
 * This source code is licensed under the MIT license.
 * You may obtain a copy of the License at 
 *      https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package tinyunittester;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;

/**
 * Automated testing tool for 'POJO' type classes, usable with JUnit
 * 
 * @author Laurent Guerin
 *
 */
public class PojoUnitTester {

	/**
	 * Inner class for POJO errors
	 */
	public class PojoException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public PojoException(String message) {
			super(message);
		}
		public PojoException(String message, Exception cause) {
			super(message, cause);
		}
		public PojoException(String message, Method method, Exception cause) {
			super(message + " method '" + method.getName() + "'" , cause);
		}
	}

	private final boolean logEnabled ;

	// Setter/Getter prefix
	private static final String SET = "set";
	private static final String GET = "get";
	private static final String IS = "is";

	/**
	 * Default constructor
	 */
	public PojoUnitTester() {
		this(false);
	}
	
	/**
	 * Constructor with log status (enabled or not)
	 * @param logEnabled
	 */
	public PojoUnitTester(boolean logEnabled) {
		super();
		this.logEnabled = logEnabled;
	}

	private void log(String msg) {
		if ( logEnabled ) {
			System.out.println("PojoTester: " + msg);
		}
	}
	private void log(Class<?> clazz, String msg) {
		log(clazz.getSimpleName() + ": " + msg);
	}
	
	/**
	 * Test test everything possible for the given class 
	 * @param clazz
	 */
	public void testAll(Class<?> clazz) {
		//testDefaultConstructor(clazz);
		testSettersAndGettersBehavior(clazz);
	}

	/**
	 * Test instance creation with the default constructor
	 * @param clazz
	 */
	public void testDefaultConstructor(Class<?> clazz) {
		log(clazz, "new instance : ");
		createInstanceWithDefaultConstructor(getDefaultConstructor(clazz));
	}

	/**
	 * Test the behavior for all the getters and setters identifiable in the class <br>
	 * Checks the value retrieved by the getter is the same as the value provided to the setter <br>
	 * The getter can be 'getXxx' or 'isXxx' (invoked only if a corresponding setter exists)
	 * @param clazz
	 */
	public void testSettersAndGettersBehavior(Class<?> clazz) {
		Object instance = createInstance(clazz);
		for (Method method : clazz.getMethods()) {
			if ( isSetterWithSingleParameter(method) ) {
				// setter found => search getter
				Method setter = method ;
				Object value1 = setValueUsingSetter(instance, setter);
				Method getter = searchGetterFromSetter(clazz, setter);
				if ( getter != null ) {
					Object value2 = getValueUsingGetter(instance, getter);
					if ( ! sameValue(value2, value1) ) {
						throw new PojoException(clazz.getSimpleName() + " : " 
								+ setter.getName() + " : " + value1 + " (" + value1.getClass().getSimpleName() + ")"
								+ " / "
								+ getter.getName() + " : " + value2 + " (" + value2.getClass().getSimpleName() + ")"   );
					}
				}
			}
		}
	}

	private boolean sameValue(Object value1, Object value2) {
		if ( value1 == null && value2 == null ) {
			return true;
		}
		else {
			if ( value1 == null || value2 == null ) { // 1 of the 2 is null
				return false;
			}
			else {
				return value1.equals(value2);
			}
		}
	}
	
	private Object setValueUsingSetter(Object instance, Method setter) {
		Parameter p = setter.getParameters()[0];
		Object value = getValue(p.getType());
		try {
			setter.invoke(instance, value);
			log(instance.getClass(), setter.getName() + "(" + value + ")");
		} catch (IllegalAccessException e) {
			throw new PojoException("Cannot set value (IllegalAccessException)", setter, e);
		} catch (IllegalArgumentException e) {
			throw new PojoException("Cannot set value (IllegalArgumentException)", setter, e);
		} catch (InvocationTargetException e) {
			throw new PojoException("Cannot set value (InvocationTargetException)", setter, e);
		}
		return value;
	}

	private Object getValueUsingGetter(Object instance, Method getter) {
		Object value = null;
		try {
			value = getter.invoke(instance);
			log(instance.getClass(), getter.getName() + "() " + value);
		} catch (IllegalAccessException e) {
			throw new PojoException("Cannot get value (IllegalAccessException)", getter, e);
		} catch (IllegalArgumentException e) {
			throw new PojoException("Cannot get value (IllegalArgumentException)", getter, e);
		} catch (InvocationTargetException e) {
			throw new PojoException("Cannot get value (InvocationTargetException)", getter, e);
		}
		return value;
	}
	
	private boolean isSetterWithSingleParameter(Method method) {
		int modifiers = method.getModifiers();
		return method.getName().startsWith(SET) 
				&& Modifier.isPublic(modifiers)
				&& ! Modifier.isAbstract(modifiers)
				&& ! Modifier.isStatic(modifiers)
				&& method.getParameterCount() == 1;
	}

	private boolean isGetter(Method method) {
		int modifiers = method.getModifiers();
		return ( method.getName().startsWith(GET) || method.getName().startsWith(IS) )
				&& Modifier.isPublic(modifiers)
				&& ! Modifier.isAbstract(modifiers)
				&& ! Modifier.isStatic(modifiers)
				&& method.getParameterCount() == 0;
	}

	private Method searchGetterFromSetter(Class<?> clazz, Method setter) {
		Method method = getMethod(clazz, changePrefix(setter.getName(), SET, GET) ); // getXxx
		if ( method == null ) {
			method = getMethod(clazz, changePrefix(setter.getName(), SET, IS) ); // isXxx
		}
		if (method != null && isGetter(method) ) {
			return method;
		}
		else {
			return null;
		}
	}

	private String changePrefix(String s, String prefix1, String prefix2) {
		String s2 = s.substring(prefix1.length());
		return prefix2 + s2;
	}

	private Object createInstance(Class<?> clazz) {
		Constructor<?> constructor = getDefaultConstructor(clazz);
		if ( constructor != null ) {
			return createInstanceWithDefaultConstructor(constructor);
		}
		else {
			// improvement : search another constructor and use it 
			throw new PojoException("Cannot create instance, no default construtor");
		}
	}

	private Constructor<?> getDefaultConstructor(Class<?> clazz) {
		try {
			return clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new PojoException("No default construtor (NoSuchMethodException)", e);
		} catch (SecurityException e) {
			throw new PojoException("Cannot get default construtor (SecurityException)", e);
		}
	}

	private Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new PojoException("Cannot get method '" + methodName + "' (SecurityException)", e);
		}
	}

	private Object createInstanceWithDefaultConstructor(Constructor<?> constructor) {
		log("new instance with default constructor");
		try {
			return constructor.newInstance();
		} catch (InstantiationException e) {
			throw new PojoException("Construtor error (InstantiationException)", e);
		} catch (IllegalAccessException e) {
			throw new PojoException("Construtor error (IllegalAccessException)", e);
		} catch (IllegalArgumentException e) {
			throw new PojoException("Construtor error (IllegalArgumentException)", e);
		} catch (InvocationTargetException e) {
			throw new PojoException("Construtor error (InvocationTargetException)", e);
		}
	}

	private Object getValue(Class<?> type) {
		if (type.isAssignableFrom(String.class)) {
            return "Z";
        } else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
            return Boolean.TRUE;
        } else if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
            return Character.valueOf('a');
        // Standard numbers
        } else if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            return Byte.valueOf((byte)4);
        } else if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
            return Short.valueOf((short)12);
        } else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return Integer.valueOf(12345);
        } else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return Long.valueOf(123456789L);
        } else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return Float.valueOf(123.45F);
        } else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return Double.valueOf(12345.6789);

        // Math types :
        } else if (type.isAssignableFrom(BigInteger.class) ) {
            return new BigInteger("12345678");
        } else if (type.isAssignableFrom(BigDecimal.class) ) {
            return new BigDecimal("123456.789");
            
        // Date-Time types (java.time.* ) :
        } else if (type.isAssignableFrom(LocalDate.class) ) {
            return LocalDate.now();
        } else if (type.isAssignableFrom(LocalDateTime.class) ) {
            return LocalDateTime.now();
        } else if (type.isAssignableFrom(LocalTime.class) ) {
            return LocalTime.now();
        } else if (type.isAssignableFrom(ZonedDateTime.class) ) {
            return ZonedDateTime.now();
        } else if (type.isAssignableFrom(OffsetDateTime.class) ) {
            return OffsetDateTime.now();
        } else if (type.isAssignableFrom(OffsetTime.class) ) {
            return OffsetTime.now();
        } else if (type.isAssignableFrom(Instant.class) ) {
            return Instant.now();
        } else if (type.isAssignableFrom(Duration.class) ) {
            return Duration.ofMinutes(45);
        } else if (type.isAssignableFrom(Period.class) ) {
            return Period.ofDays(12); // a period of 12 days 
        } else if (type.isAssignableFrom(Year.class) ) { // a year 
            return Year.now();
        } else if (type.isAssignableFrom(YearMonth.class) ) { // combination of year + month
            return YearMonth.now();
        } else if (type.isAssignableFrom(Month.class) ) { // a month of the year (from January to December) 
            return Month.AUGUST;
        } else if (type.isAssignableFrom(MonthDay.class) ) { // combination of month + day
            return MonthDay.now();
        } else if (type.isAssignableFrom(DayOfWeek.class) ) { // a day of the week (from Monday to Sunday) 
            return DayOfWeek.SATURDAY;
        } else if (type.isAssignableFrom(ZoneOffset.class) ) {
            return ZoneOffset.UTC;

        // Util types (java.util.*) 
	    } else if (type.isAssignableFrom(UUID.class)) {
	        return UUID.randomUUID();
	    } else if (type.isAssignableFrom(Currency.class)) {
	        return Currency.getInstance("EUR");
	        
        // Text types (java.text.*) 
	    } else if (type.isAssignableFrom(SimpleDateFormat.class)) {
	        return new SimpleDateFormat();
	    } else if (type.isAssignableFrom(MessageFormat.class)) {
	        return new MessageFormat("{0} days, {1} hours, {2} minutes)");
		        
        // Network types (java.net.*) 
	    } else if (type.isAssignableFrom(URL.class)) {
	        try {
				return new URL("http://www.example.com/");
			} catch (MalformedURLException e) {
				return null;
			}
	    } else if (type.isAssignableFrom(URI.class)) {
			return URI.create("http://www.example.com/");
		        
        // SQL types :
        } else if (type.isAssignableFrom(java.sql.Date.class)) {
            new java.sql.Date(2000000000L); 
        } else if (type.isAssignableFrom(java.sql.Time.class)) {
            new java.sql.Time(3000000000L); 
        } else if (type.isAssignableFrom(java.sql.Timestamp.class)) {
            new java.sql.Timestamp(4000000000L); 
            
        // old types (deprecated but still in use sometimes) :
        } else if (type.isAssignableFrom(java.util.Date.class)) {
            return new java.util.Date();
	    }
		
		// add your specific types here if necessary
		
		// unknown type => return null
    	return null;
	}

}
