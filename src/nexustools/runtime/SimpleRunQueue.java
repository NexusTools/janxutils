/*
 * janxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * janxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with janxutils.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package nexustools.runtime;

import java.util.List;
import nexustools.concurrent.Accessor;
import nexustools.concurrent.ConcurrentList;

/**
 *
 * @author katelyn
 */
public class SimpleRunQueue<R extends Runnable, F extends QueueFuture<R>> extends RunQueue<R, F, RunThread> {

	final ConcurrentList<RunThread> activeThreads = new ConcurrentList();
	final ConcurrentList<RunThread> idleThreads = new ConcurrentList();
	private final ConcurrentList<F> tasks = new ConcurrentList();
	public SimpleRunQueue(String name, int threads) {
		super(name);
		if(threads < 1)
			threads = Runtime.getRuntime().availableProcessors();
		while(threads > 0) {
			RunThread runThread = new RunThread(name + "[Worker" + threads + "]", this);
			activeThreads.add(runThread);
			idleThreads.add(runThread);
			threads --;
		}
	}
	public SimpleRunQueue(String name) {
		this(name, -1);
	}
	public SimpleRunQueue(int threads) {
		this(null, threads);
	}
	public SimpleRunQueue() {
		this(null, -1);
	}

	@Override
	public F nextFuture(RunThread runThread) {
		return tasks.read(new IfReader<Accessor<List<F>>, F>() {
			@Override
			public F read(Accessor<List<F>> value) {
				return value.internal().remove(0);
			}
		});
	}

	@Override
	protected void push(F future) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<R> internal() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void init(List<R> object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isset() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
