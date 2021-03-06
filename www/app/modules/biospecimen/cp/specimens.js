
angular.module('os.biospecimen.cp.specimens', ['os.biospecimen.models'])
  .controller('CpSpecimensCtrl', function(
    $scope, $state, $stateParams, $timeout, $modal,
    cp, events, specimenRequirements,
    Specimen, SpecimenRequirement, PvManager, Alerts) {

    if (!$stateParams.eventId && !!events && events.length > 0) {
      $state.go('cp-detail.specimen-requirements', {eventId: events[0].id});
      return;
    }

    function init() {
      $scope.cp = cp;
      $scope.events = events;
      $scope.eventId = $stateParams.eventId;
      $scope.selectEvent({id: $stateParams.eventId});

      $scope.specimenRequirements = Specimen.flatten(specimenRequirements);

      $scope.view = 'list_sr';
      $scope.sr = {};
      $scope.childReq = {};
      $scope.errorCode = '';
    }

    function addToSrList(sr) {
      specimenRequirements.push(sr);
      $scope.specimenRequirements = Specimen.flatten(specimenRequirements);
    }

    function updateSrList(sr) {
      var result = findSr(specimenRequirements, sr.id);
      angular.extend(result.sr, sr);
      $scope.specimenRequirements = Specimen.flatten(specimenRequirements);
    }

    function deleteFromSrList(sr) {
      var result = findSr(specimenRequirements, sr.id);
      result.list.splice(result.idx, 1);
      $scope.specimenRequirements = Specimen.flatten(specimenRequirements);
    }

    function findSr(srList, srId) {
      if (!srList) {
        return undefined;
      }

      for (var i = 0; i < srList.length; ++i) {
        if (srList[i].id == srId) {
          return {list: srList, sr: srList[i], idx: i};
        }
        var result = findSr(srList[i].children, srId);
        if (!!result) {
          return result;
        }
      }

      return undefined;
    }

    function addChildren(parent, children) {
      if (!parent.children) {
        parent.children = [];
      }

      angular.forEach(children, function(child) {
        parent.children.push(child);
      });

      $scope.specimenRequirements = Specimen.flatten(specimenRequirements);
    };

    var pvsLoaded = false;
    function loadPvs() {
      if (pvsLoaded) {
        return;
      }

      $scope.specimenClasses = PvManager.getPvs('specimen-class');
      $scope.specimenTypes = [];

      $scope.$watch('sr.specimenClass', function(newVal, oldVal) {
        if (!newVal || newVal == oldVal || !oldVal) {
          return;
        }
        $scope.sr.type = '';
      });

      $scope.anatomicSites = PvManager.getPvs('anatomic-site');
      $scope.lateralities = PvManager.getPvs('laterality');
      $scope.pathologyStatuses = PvManager.getPvs('pathology-status');
      $scope.storageTypes = PvManager.getPvs('storage-type');
      $scope.collectionProcs = PvManager.getPvs('collection-procedure');
      $scope.collectionContainers = PvManager.getPvs('collection-container');
      pvsLoaded = true;
    };

    $scope.loadSpecimenTypes = function(specimenClass) {
      $scope.specimenTypes = PvManager.getPvsByParent('specimen-class', specimenClass);
    };

    $scope.openSpecimenNode = function(sr) {
      sr.isOpened = true;
    };

    $scope.closeSpecimenNode = function(sr) {
      sr.isOpened = false;
    };

    $scope.showAddSr = function() {
      $scope.view = 'addedit_sr';
      $scope.sr = new SpecimenRequirement({eventId: $scope.eventId});
      loadPvs();
    };

    $scope.showEditSr = function(sr) {
      $scope.sr = angular.copy(sr);
      delete $scope.sr.depth;
      delete $scope.sr.hasChildren;
      delete $scope.sr.children;
      delete $scope.sr.isOpened;
      delete $scope.sr.parent;

      if (sr.isAliquot()) {
        $scope.view = 'addedit_aliquot';
        $scope.parentSr = sr.parent;
        $scope.childReq = $scope.sr;
      } else if (sr.isDerivative()) {
        $scope.view = 'addedit_derived';
        $scope.parentSr = sr.parent;
        $scope.childReq = $scope.sr;
      } else {
        $scope.view = 'addedit_sr';
      }
      loadPvs();
    };

    $scope.viewSr = function(sr) {
      $scope.view = 'view_sr';
      $scope.parentSr = sr.parent;
      $scope.childReq = sr;
    };

    $scope.revertEdit = function() {
      $scope.view = 'list_sr';
      $scope.parentSr = null;
      $scope.childReq = {};
      $scope.sr = {};
    };

    $scope.createSr = function() {
      $scope.sr.$saveOrUpdate().then(
        function(result) {
          addToSrList(result);
          $scope.view = 'list_sr';
        }
      );
    };

    $scope.updateSr = function() {
      $scope.sr.$saveOrUpdate().then(
        function(result) {
          updateSrList(result);
          $scope.revertEdit();
        }
      );
    };

    ////////////////////////////////////////////////
    //
    //  Aliquot logic
    //
    ////////////////////////////////////////////////
    $scope.showCreateAliquots = function(sr) {
      if (sr.availableQty() == 0) {
        Alerts.error('srs.errors.insufficient_qty');
        return;
      }

      $scope.parentSr = sr;
      $scope.view = 'addedit_aliquot';
      $scope.childReq = {};
      loadPvs();
    };

    $scope.createAliquots = function() {
      var spec = $scope.childReq;
      var availableQty = $scope.parentSr.availableQty();

      if (!!spec.qtyPerAliquot && !!spec.noOfAliquots) {
        var requiredQty = spec.qtyPerAliquot * spec.noOfAliquots;
        if (requiredQty > availableQty) {
          Alerts.error("srs.errors.insufficient_qty");
          return;
        }
      } else if (!!spec.qtyPerAliquot) {
        spec.noOfAliquots = Math.floor(availableQty / spec.qtyPerAliquot);
      } else if (!!spec.noOfAliquots) {
        spec.qtyPerAliquot = Math.round(availableQty / spec.noOfAliquots * 10000) / 10000;
      }

      $scope.parentSr.createAliquots(spec).then(
        function(aliquots) {
          addChildren($scope.parentSr, aliquots);
          $scope.parentSr.isOpened = true;

          $scope.childReq = {};
          $scope.parentSr = undefined;
          $scope.view = 'list_sr';
        }
      );
    };

    ////////////////////////////////////////////////
    //
    //  Derivative logic
    //
    ////////////////////////////////////////////////
    $scope.showCreateDerived = function(sr) {
      $scope.parentSr = sr;
      $scope.view = 'addedit_derived';
      $scope.childReq = {};
      loadPvs();
    };

    $scope.createDerivative = function() {
      $scope.parentSr.createDerived($scope.childReq).then(
        function(derived) {
          addChildren($scope.parentSr, [derived]);
          $scope.parentSr.isOpened = true;

          $scope.childReq = {};
          $scope.parentSr = undefined;
          $scope.view = 'list_sr';
        }
      );
    };

    $scope.copyRequirement = function(sr) {
      var aliquotReq = {noOfAliquots: 1, qtyPerAliquot: sr.initialQty};
      if (sr.isAliquot() && !sr.parent.hasSufficientQty(aliquotReq)) {
        Alerts.error('srs.errors.insufficient_qty');
        return;
      }
      
      sr.copy().then(
        function(result) {
          if (sr.parent) {
            addChildren(sr.parent, [result]);
          } else {
            addToSrList(result);
          }
        }
      );
    };

    $scope.deleteRequirement = function(sr) {
      var modalInstance = $modal.open({
        templateUrl: 'delete_sr.html',
        controller: function($scope, $modalInstance) {
          $scope.yes = function() {
            $modalInstance.close(true);
          }

          $scope.no = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });

      modalInstance.result.then(
        function() {
          sr.delete().then(
            function() {
              deleteFromSrList(sr);
            }
          );
        }
      );
    }; 

    init();
  });
