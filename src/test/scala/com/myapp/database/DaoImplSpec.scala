package com.myapp.database

import org.scalatest._

@DoNotDiscover
class DaoImplSpec
    extends Suites(
      new OrgDaoImplSpec,
      new PersonDaoImplSpec,
      new WardrobeDaoImplSpec,
      new HookDaoImplSpec
    )
