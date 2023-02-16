package org.zerock.api01.repository.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.zerock.api01.domain.QTodo;
import org.zerock.api01.domain.Todo;
import org.zerock.api01.dto.PageRequestDTO;
import org.zerock.api01.dto.TodoDTO;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;

public class TodoSearchImpl extends QuerydslRepositorySupport 
	implements TodoSearch {

	//하위 클래스 생성자는 상위 클래스 생성자를 호출해야 합니다.
	public TodoSearchImpl() {
		super(Todo.class);
	}

	@Override
	public Page<TodoDTO> list(PageRequestDTO pageRequestDTO) {
		QTodo todo = QTodo.todo;
		JPQLQuery<Todo> query = from(todo);
		
		if(pageRequestDTO.getFrom() != null && pageRequestDTO.getTo() != null) {
			//todo List 기간 from~to가 있다면?
			BooleanBuilder fromToBuilder = new BooleanBuilder();
			fromToBuilder.and(todo.dueDate.goe(pageRequestDTO.getFrom()));
			fromToBuilder.and(todo.dueDate.loe(pageRequestDTO.getTo()));
			//where dueDate < pageReq.getTo() and dueDate > pageReq.getFrom()
			query.where(fromToBuilder); // ) closing
		}
		
		if(pageRequestDTO.getCompleted() != null) {
			query.where(todo.complete.eq(pageRequestDTO.getCompleted()));
		}
		
		if(pageRequestDTO.getKeyword() != null) {
			query.where(todo.title.contains(pageRequestDTO.getKeyword()));
		}
		
		this.getQuerydsl().applyPagination(pageRequestDTO.getPageable("tno"), query);
		
		JPQLQuery<TodoDTO> dtoQuery 
			= query.select(Projections.bean(TodoDTO.class
						,todo.tno
						,todo.title
						,todo.dueDate
						,todo.complete
						,todo.writer));
		List<TodoDTO> list = dtoQuery.fetch();
		long count = dtoQuery.fetchCount();
		
		return new PageImpl<>(list,pageRequestDTO.getPageable("tno"),count);
	}

}
