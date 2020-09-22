package edu.kit.ipd.sdq.respond.repository

import javax.persistence.EntityManagerFactory

typealias ProcessId = Int
typealias ProcessContent = String

class Repository(private val sessionFactory: EntityManagerFactory) {

    fun registerProcess(process: Process): Process {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        entityManager.persist(process)
        entityManager.transaction.commit()
        entityManager.close()
        return process
    }

    fun getProcess(processId: ProcessId, plant: Plant): Process {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("from Process  where plant_id = :plant and id = :id", Process::class.java)
        query.setParameter("plant", plant.id)
        query.setParameter("id", processId)
        query.executeUpdate()
        return query.singleResult
    }

    fun removeProcess(processId: ProcessId, plant: Plant) {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        val query = entityManager.createQuery("delete from Process where plant_id = :plant and id = :id")
        query.setParameter("plant", plant.id)
        query.setParameter("id", processId)
        query.executeUpdate()
        entityManager.close()
    }

    fun getProcesses(plant: Plant): List<ProcessDescriptor> {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("from Process where plant_id = :plant", Process::class.java)
        query.setParameter("plant", plant.id)
        return query.resultList.map { it.processDescriptor }
    }

    fun removeAllProcesses(plant: Plant) {
        val entityManager = sessionFactory.createEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        entityManager.transaction.begin()
        val query = entityManager.createQuery("delete from Process where plant_id = :plant")
        query.setParameter("plant", plant.id)
        query.executeUpdate()
        entityManager.close()
    }

    fun updateProcess(processId: ProcessId, plant: Plant, processContent: ProcessContent): Process {
        val entityManager = sessionFactory.createEntityManager()
        entityManager.transaction.begin()
        val process = getProcess(processId, plant)
        process.source = processContent
        entityManager.transaction.commit()
        entityManager.close()
        return process
    }

    fun getPlant(plantPath: String): Plant {
        val entityManager = sessionFactory.createEntityManager()
        val query = entityManager.createQuery("select p from Plant p where path = :path", Plant::class.java)
        query.setParameter("path", plantPath)
        return query.singleResult
    }
}


