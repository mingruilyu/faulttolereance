package api;

/**
 * Job interface is to put the root task in the task queue of Space.
 * 
 * @author Gongxl
 *
 * @param <T>
 *            the return type of the task
 */
public interface Job<T> {

	public Task<T> toTask(Space space);
}
