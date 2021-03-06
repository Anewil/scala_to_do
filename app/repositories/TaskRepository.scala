package repositories

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import anorm._
import anorm.SqlParser._
import play.api.db._
import models.Task


@javax.inject.Singleton
class TaskRepository @Inject()(database: Database)(implicit ec: ExecutionContext) {

  private val task = {
    get[Option[Long]]("id") ~
      get[String]("name") ~
      get[String]("comments") ~
    get[String]("target") ~
      get[Int]("completed") map {
      case id ~ name ~ comments ~ target ~ completed => Task(id, name, comments,target, completed == 1)
    }
  }

  private val DB = database

  def getById(id: Long): Option[Task] = {
    DB.withConnection { implicit c =>
      SQL("select * from task where id = {id}").on('id -> id).as(task.singleOpt)
    }
  }


  def all(): List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task").as(task *)
  }

  def create(task: Task) {
    DB.withConnection { implicit c =>
      SQL("""
        insert into task (name, comments, target, completed) values (
          {name}, {comments},{target}, {completed}
        )
        """).bind(task).executeInsert()
    }
  }

  def update(id: Long, task: Task) {
    DB.withConnection { implicit c =>
      SQL("""
        UPDATE task SET name={name}, comments={comments},target={target}, completed={completed}
        WHERE id = {id}
        """).bind(task.copy(id = Some(id))).executeUpdate()
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit c =>
      SQL("""
        delete from task where id = {id}
        """).on('id -> id).executeUpdate()
    }
  }

}