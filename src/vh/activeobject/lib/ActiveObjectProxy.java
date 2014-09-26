package vh.activeobject.lib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class ActiveObjectProxy {

	private static class DispatchInvocationHandler implements InvocationHandler {
		private final Object delegate;
		private final ExecutorService scheduler;

		public DispatchInvocationHandler(Object delegate,
		    ExecutorService executorService) {
			this.delegate = delegate;
			this.scheduler = executorService;
		}

		private String makeDelegateMethodName(final Method method,
		    final Object[] arg) {
			String name = method.getName();
			name = "do" + Character.toUpperCase(name.charAt(0)) 
					+ name.substring(1);

			return name;
		}

		@Override
		public Object invoke(final Object proxy, final Method method,
		    final Object[] args) throws Throwable {

			Object returnValue = null;
			final Object delegate = this.delegate;
			final Method delegateMethod;
			
			//如果拦截到的被调用方法是异步方法，则将其转发到相应的doXXX方法
			if (Future.class.isAssignableFrom(method.getReturnType())) {
				delegateMethod = delegate.getClass().getMethod(
																		makeDelegateMethodName(method, args),
																		method.getParameterTypes());

				final ExecutorService scheduler = this.scheduler;
				
				Callable<Object> methodRequest = new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						Object rv = null;

						try {
							rv = delegateMethod.invoke(
																			delegate, args);
						} catch (IllegalArgumentException e) {
							throw new Exception(e);
						} catch (IllegalAccessException e) {
							throw new Exception(e);
						} catch (InvocationTargetException e) {
							throw new Exception(e);
						}
						return rv;
					}
				};
				Future<Object> future = scheduler.submit(methodRequest);
				returnValue = future;

			} else {
				
				//若拦截到的方法调用不是异步方法，则直接转发
				delegateMethod = delegate.getClass()
							.getMethod(method.getName(),method.getParameterTypes());
				returnValue = delegateMethod.invoke(delegate, args);
			}

			return returnValue;
		}
	}

	/**
	 * 生成一个实现指定接口的Active Object proxy实例。
	 * 对interf所定义的异步方法的调用会被装发到servant的相应doXXX方法。
	 * @param interf 要实现的Active Object接口
	 * @param servant Active Object的Servant参与者实例
	 * @param scheduler Active Object的Scheduler参与者实例
	 * @return Active Object的Proxy参与者实例
	 */
	public static <T> T newInstance(Class<T> interf, Object servant,
	    ExecutorService scheduler) {

		@SuppressWarnings("unchecked")
		T f = (T) Proxy.newProxyInstance(interf.getClassLoader(),
																new Class[] { interf }, 
																new DispatchInvocationHandler(servant, scheduler));

		return f;
	}
}